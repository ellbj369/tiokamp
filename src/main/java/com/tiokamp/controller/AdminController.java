package com.tiokamp.controller;

import com.tiokamp.model.Event;
import com.tiokamp.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ScoreService scoreService;

    @GetMapping
    public String adminPage(Model model) {
        boolean needsFacit = Arrays.stream(Event.values())
                .anyMatch(e -> e.getDirection() == Event.Direction.CLOSEST)
                && scoreService.getMakaroniFacit() == null;
        model.addAttribute("events", Event.values());
        model.addAttribute("rows", scoreService.getAdminRows());
        model.addAttribute("resultsCalculated", scoreService.resultsCalculated());
        model.addAttribute("withScores", scoreService.countUsersWithScores());
        model.addAttribute("needsFacit", needsFacit);
        model.addAttribute("makaroniFacit", scoreService.getMakaroniFacit());
        return "admin";
    }

    @PostMapping("/calculate")
    public String calculate(RedirectAttributes redirectAttributes) {
        try {
            int participants = scoreService.calculateFinalResults();
            redirectAttributes.addFlashAttribute("success",
                    "Slutresultatet är beräknat för " + participants + " deltagare.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/clear-results")
    public String clearResults(RedirectAttributes redirectAttributes) {
        scoreService.clearFinalResults();
        redirectAttributes.addFlashAttribute("success",
                "Slutresultatet är borttaget — poäng kan matas in igen.");
        return "redirect:/admin";
    }
}
