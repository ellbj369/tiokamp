package com.tiokamp.service;

import com.tiokamp.dto.EventCategoryDto;
import com.tiokamp.dto.LatestScoreDto;
import com.tiokamp.dto.TopEntryDto;
import com.tiokamp.dto.UserScoreDto;
import com.tiokamp.model.Score;
import com.tiokamp.model.User;
import com.tiokamp.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm, dd MMM");

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

    @Transactional(readOnly = true)
    public UserScoreDto getUserScores(String username) {
        User user = userService.findByUsername(username);
        return scoreRepository.findByUserId(user.getId()).map(s -> {
            UserScoreDto dto = new UserScoreDto();
            dto.setEvent1(s.getEvent1());
            dto.setEvent2(s.getEvent2());
            dto.setEvent3(s.getEvent3());
            dto.setEvent4(s.getEvent4());
            dto.setEvent5(s.getEvent5());
            dto.setEvent6(s.getEvent6());
            dto.setEvent7(s.getEvent7());
            dto.setEvent8(s.getEvent8());
            dto.setEvent9(s.getEvent9());
            dto.setEvent10(s.getEvent10());
            dto.setTotalScore(s.getTotalScore());
            return dto;
        }).orElse(new UserScoreDto());
    }

    @Transactional(readOnly = true)
    public LatestScoreDto getLatestScore() {
        List<Score> recent = scoreRepository.findMostRecentlyUpdated(PageRequest.of(0, 1));
        if (recent.isEmpty()) return null;

        Score s = recent.get(0);
        return new LatestScoreDto(
                s.getUser().getUsername(),
                s.getUser().getProfilePicture(),
                s.getLastUpdatedEvent(),
                s.getLastUpdatedValue(),
                s.getTotalScore(),
                s.getLastUpdatedAt() != null ? s.getLastUpdatedAt().format(FORMATTER) : ""
        );
    }

    @Transactional(readOnly = true)
    public List<EventCategoryDto> getLeaderboard() {
        PageRequest top3 = PageRequest.of(0, 3);
        List<EventCategoryDto> categories = new ArrayList<>();

        String[] eventNames = {
            "100m Sprint", "Long Jump", "Shot Put", "High Jump", "400m",
            "110m Hurdles", "Discus", "Pole Vault", "Javelin", "1500m"
        };

        List<List<Score>> allEventScores = List.of(
            scoreRepository.findTop3ByEvent1(top3),
            scoreRepository.findTop3ByEvent2(top3),
            scoreRepository.findTop3ByEvent3(top3),
            scoreRepository.findTop3ByEvent4(top3),
            scoreRepository.findTop3ByEvent5(top3),
            scoreRepository.findTop3ByEvent6(top3),
            scoreRepository.findTop3ByEvent7(top3),
            scoreRepository.findTop3ByEvent8(top3),
            scoreRepository.findTop3ByEvent9(top3),
            scoreRepository.findTop3ByEvent10(top3)
        );

        for (int i = 0; i < 10; i++) {
            List<Score> scores = allEventScores.get(i);
            final int eventIndex = i + 1;
            List<TopEntryDto> entries = new ArrayList<>();

            for (int j = 0; j < scores.size(); j++) {
                Score s = scores.get(j);
                Double val = getEventValue(s, eventIndex);
                entries.add(new TopEntryDto(
                        j + 1,
                        s.getUser().getUsername(),
                        s.getUser().getProfilePicture(),
                        val
                ));
            }
            categories.add(new EventCategoryDto(eventNames[i], entries));
        }
        return categories;
    }

    private Double getEventValue(Score s, int n) {
        return switch (n) {
            case 1  -> s.getEvent1();
            case 2  -> s.getEvent2();
            case 3  -> s.getEvent3();
            case 4  -> s.getEvent4();
            case 5  -> s.getEvent5();
            case 6  -> s.getEvent6();
            case 7  -> s.getEvent7();
            case 8  -> s.getEvent8();
            case 9  -> s.getEvent9();
            case 10 -> s.getEvent10();
            default -> null;
        };
    }
}
