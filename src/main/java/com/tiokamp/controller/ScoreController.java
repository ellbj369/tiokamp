package com.tiokamp.controller;

import com.tiokamp.model.Event;
import com.tiokamp.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    /** Score cards page */
    @GetMapping("/scores")
    public String scoresPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("events", Event.values());
        model.addAttribute("current", scoreService.getUserScoreDisplay(userDetails.getUsername()));
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("locked", scoreService.resultsCalculated());
        return "scores";
    }

    /** Save a single event score — called per card via JS fetch */
    @PostMapping("/api/scores/{eventNumber}")
    @ResponseBody
    public ResponseEntity<?> saveScore(
            @PathVariable int eventNumber,
            @RequestBody Map<String, Double> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (eventNumber < 1 || eventNumber > Event.count()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ogiltig gren."));
        }
        if (scoreService.resultsCalculated()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Tävlingen är avslutad — slutresultatet är redan beräknat."));
        }
        Double value = body.get("value");
        if (value == null || value.isNaN() || value.isInfinite() || value < 0 || value > 100000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ogiltigt värde — ange ett tal mellan 0 och 100 000."));
        }
        scoreService.saveEventScore(userDetails.getUsername(), eventNumber, value);
        return ResponseEntity.ok(Map.of("saved", true));
    }

    /** Leaderboard page */
    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model) {
        boolean resultsCalculated = scoreService.resultsCalculated();
        model.addAttribute("categories", scoreService.getLeaderboard());
        model.addAttribute("latest", scoreService.getLatestScore());
        model.addAttribute("resultsCalculated", resultsCalculated);
        if (resultsCalculated) {
            model.addAttribute("finalStandings", scoreService.getFinalStandings());
        }
        return "leaderboard";
    }

    /** Leaderboard latest score — polled by JS every 10s */
    @GetMapping("/api/leaderboard/latest")
    @ResponseBody
    public ResponseEntity<?> getLatest() {
        var latest = scoreService.getLatestScore();
        if (latest == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(latest);
    }
}
