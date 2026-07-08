package com.tiokamp.controller;

import com.tiokamp.service.ScoreService;
import lombok.RequiredArgsConstructor;
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
        var scores = scoreService.getUserScores(userDetails.getUsername());
        model.addAttribute("scores", scores);
        model.addAttribute("username", userDetails.getUsername());
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
        if (eventNumber < 1 || eventNumber > 10) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid event number"));
        }
        Double value = body.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing value"));
        }
        var score = scoreService.saveEventScore(userDetails.getUsername(), eventNumber, value);
        return ResponseEntity.ok(Map.of("total", score.getTotalScore(), "saved", true));
    }

    /** Leaderboard page */
    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model) {
        model.addAttribute("categories", scoreService.getLeaderboard());
        model.addAttribute("latest", scoreService.getLatestScore());
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
