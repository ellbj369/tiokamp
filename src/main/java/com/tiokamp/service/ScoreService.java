package com.tiokamp.service;

import com.tiokamp.dto.AdminRowDto;
import com.tiokamp.dto.EventCategoryDto;
import com.tiokamp.dto.FinalStandingDto;
import com.tiokamp.dto.LatestScoreDto;
import com.tiokamp.dto.TopEntryDto;
import com.tiokamp.config.AdminWhitelist;
import com.tiokamp.model.Event;
import com.tiokamp.model.Score;
import com.tiokamp.model.User;
import com.tiokamp.repository.ScoreRepository;
import com.tiokamp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SettingsService settingsService;
    private final AdminWhitelist adminWhitelist;
    private final ScoreMessages scoreMessages;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm, d MMM", Locale.forLanguageTag("sv-SE"));

    @Transactional
    public Score saveEventScore(String username, int eventNumber, Double value) {
        User user = userService.findByUsername(username);

        Score score = scoreRepository.findByUserId(user.getId()).orElseGet(() -> {
            Score s = new Score();
            s.setUser(user);
            return s;
        });

        score.setEventByNumber(eventNumber, value);
        return scoreRepository.save(score);
    }

    /** Raw values per event number, pre-formatted for display; "—" when missing. */
    @Transactional(readOnly = true)
    public Map<Integer, String> getUserScoreDisplay(String username) {
        User user = userService.findByUsername(username);
        Score score = scoreRepository.findByUserId(user.getId()).orElse(null);
        Map<Integer, String> values = new LinkedHashMap<>();
        for (Event event : Event.values()) {
            values.put(event.getNumber(),
                    fmt(score != null ? score.getEventByNumber(event.getNumber()) : null));
        }
        return values;
    }

    @Transactional(readOnly = true)
    public LatestScoreDto getLatestScore() {
        List<Score> recent = scoreRepository.findMostRecentlyUpdated(PageRequest.of(0, 1));
        if (recent.isEmpty()) return null;

        Score s = recent.get(0);
        Event event = Event.byDisplayName(s.getLastUpdatedEvent());
        boolean hidden = event != null && event.isHiddenOnLeaderboard();
        long seed = (s.getUser().getUsername() + "|" + s.getLastUpdatedAt()).hashCode();
        // For a secret guessing event, don't leak the value or a placement hint.
        Integer placement = hidden ? null : latestPlacement(event, s.getLastUpdatedValue());
        String message = hidden ? "Gissningen hålls hemlig tills spelet är slut 🤫"
                                : scoreMessages.forPlacement(placement, seed);

        return new LatestScoreDto(
                s.getUser().getUsername(),
                s.getUser().getProfilePicture(),
                s.getLastUpdatedEvent(),
                hidden ? null : fmt(s.getLastUpdatedValue()),
                s.getTotalScore(),
                s.getLastUpdatedAt() != null ? s.getLastUpdatedAt().format(FORMATTER) : "",
                message,
                hidden
        );
    }

    /** 1-based placement of a value within its event (direction-aware), or null if unrankable. */
    private Integer latestPlacement(Event event, Double value) {
        if (event == null || value == null) return null;
        Double facit = settingsService.getMakaroniFacit();
        if (event.getDirection() == Event.Direction.CLOSEST && facit == null) return null;
        double mine = event.rankingValue(value, facit);
        int better = 0;
        for (Score other : scoreRepository.findAllWithUser()) {
            Double otherValue = other.getEventByNumber(event.getNumber());
            if (otherValue == null) continue;
            if (event.rankingValue(otherValue, facit) > mine) better++;
        }
        return better + 1;
    }

    /**
     * Top 3 per event, ranked by each event's own direction (computed in Java —
     * CLOSEST events can't be ranked in SQL since they depend on the facit).
     */
    @Transactional(readOnly = true)
    public List<EventCategoryDto> getLeaderboard() {
        List<Score> allScores = scoreRepository.findAllWithUser();
        List<EventCategoryDto> categories = new ArrayList<>();
        Double makaroniFacit = settingsService.getMakaroniFacit();

        for (Event event : Event.values()) {
            // Secret guessing events (e.g. the macaroni jar) aren't shown here.
            if (event.isHiddenOnLeaderboard()) continue;
            boolean rankable = event.getDirection() != Event.Direction.CLOSEST
                    || makaroniFacit != null;

            List<TopEntryDto> top3 = new ArrayList<>();
            if (rankable) {
                List<Score> ranked = allScores.stream()
                        .filter(s -> s.getEventByNumber(event.getNumber()) != null)
                        .sorted(Comparator
                                .comparingDouble((Score s) -> event.rankingValue(
                                        s.getEventByNumber(event.getNumber()), makaroniFacit))
                                .reversed()
                                .thenComparing(s -> s.getUser().getUsername(),
                                        String.CASE_INSENSITIVE_ORDER))
                        .limit(3)
                        .toList();
                for (int i = 0; i < ranked.size(); i++) {
                    Score s = ranked.get(i);
                    top3.add(new TopEntryDto(
                            i + 1,
                            s.getUser().getUsername(),
                            s.getUser().getProfilePicture(),
                            fmt(s.getEventByNumber(event.getNumber()))
                    ));
                }
            }

            String emptyMessage = rankable
                    ? "Inga tävlande än."
                    : "Rankas när facit är inlagt.";
            categories.add(new EventCategoryDto(event.getDisplayName(), top3, emptyMessage));
        }
        return categories;
    }

    // ── Final placement results ──────────────────────────

    @Transactional(readOnly = true)
    public boolean resultsCalculated() {
        return scoreRepository.existsByFinalRankIsNotNull();
    }

    /**
     * One-time placement calculation over ALL registered users (N = number of users).
     * Each raw value is first mapped through the event's direction so that higher
     * always means better, then the calculator assigns 1..N points per event.
     * Throws IllegalStateException if a CLOSEST event's facit isn't set.
     */
    @Transactional
    public int calculateFinalResults() {
        List<User> users = userRepository.findAll();
        Map<Long, Score> scoreByUserId = new HashMap<>();
        Map<Long, Double[]> rankingByUserId = new LinkedHashMap<>();
        Event[] events = Event.values();
        Double makaroniFacit = settingsService.getMakaroniFacit();

        for (User user : users) {
            Score score = scoreRepository.findByUserId(user.getId()).orElseGet(() -> {
                Score s = new Score();
                s.setUser(user);
                return s;
            });
            scoreByUserId.put(user.getId(), score);

            Double[] rankingValues = new Double[events.length];
            for (int i = 0; i < events.length; i++) {
                rankingValues[i] = events[i].rankingValue(
                        score.getEventByNumber(events[i].getNumber()), makaroniFacit);
            }
            rankingByUserId.put(user.getId(), rankingValues);
        }

        Map<Long, ResultCalculator.Standing> standings = ResultCalculator.calculate(rankingByUserId);

        for (User user : users) {
            Score score = scoreByUserId.get(user.getId());
            ResultCalculator.Standing standing = standings.get(user.getId());
            score.applyFinalResult(standing.eventPoints(), standing.totalPoints(), standing.rank());
            scoreRepository.save(score);
        }
        return users.size();
    }

    /** Removes the calculated results so the game opens up again. */
    @Transactional
    public void clearFinalResults() {
        for (Score score : scoreRepository.findAll()) {
            score.clearFinalResult();
            scoreRepository.save(score);
        }
    }

    @Transactional(readOnly = true)
    public List<FinalStandingDto> getFinalStandings() {
        return scoreRepository.findFinalStandings().stream()
                .map(s -> new FinalStandingDto(
                        s.getFinalRank(),
                        s.getUser().getUsername(),
                        s.getUser().getProfilePicture(),
                        fmt(s.getPlacementTotal())))
                .toList();
    }

    /** All players with raw values (and points once calculated) for the admin page. */
    @Transactional(readOnly = true)
    public List<AdminRowDto> getAdminRows() {
        boolean calculated = resultsCalculated();
        List<User> users = userRepository.findAll();
        // Load every score in one query (join-fetching the user) instead of one
        // query per participant.
        Map<Long, Score> scoreByUserId = new HashMap<>();
        for (Score s : scoreRepository.findAllWithUser()) {
            scoreByUserId.put(s.getUser().getId(), s);
        }
        List<AdminRowDto> rows = new ArrayList<>();

        for (User user : users) {
            Score score = scoreByUserId.get(user.getId());
            List<String> values = new ArrayList<>(Event.count());
            List<String> points = calculated ? new ArrayList<>(Event.count()) : null;
            for (Event event : Event.values()) {
                values.add(fmt(score != null ? score.getEventByNumber(event.getNumber()) : null));
                if (calculated) {
                    points.add(fmt(score != null ? score.getPointsByNumber(event.getNumber()) : null));
                }
            }
            boolean listed = adminWhitelist.isListed(user.getUsername());
            rows.add(new AdminRowDto(
                    score != null ? score.getFinalRank() : null,
                    user.getUsername(),
                    user.getProfilePicture(),
                    values,
                    points,
                    score != null ? score.getFilledCount() : 0,
                    calculated && score != null ? fmt(score.getPlacementTotal()) : null,
                    listed || user.isAdmin(),
                    listed
            ));
        }

        if (calculated) {
            rows.sort(Comparator.comparing(AdminRowDto::getRank,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else {
            rows.sort(Comparator.comparing(row -> row.getUsername().toLowerCase(Locale.ROOT)));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public long countUsersWithScores() {
        return scoreRepository.findAll().stream().filter(s -> s.getFilledCount() > 0).count();
    }

    public Double getMakaroniFacit() {
        return settingsService.getMakaroniFacit();
    }

    /** Swedish display format: no trailing zeros, decimal comma, "—" for missing. */
    static String fmt(Double value) {
        if (value == null) return "—";
        if (value % 1 == 0 && Math.abs(value) < 1e15) {
            return String.valueOf(value.longValue());
        }
        return String.valueOf(value).replace('.', ',');
    }
}
