package com.tiokamp.controller;

import com.tiokamp.model.Event;
import com.tiokamp.service.ScoreService;
import com.tiokamp.service.SettingsService;
import com.tiokamp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ScoreService scoreService;
    private final UserService userService;
    private final SettingsService settingsService;

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

    @PostMapping("/makaroni-facit")
    public String setMakaroniFacit(@RequestParam String facit, RedirectAttributes redirectAttributes) {
        try {
            double value = Double.parseDouble(facit.trim().replace(',', '.'));
            settingsService.setMakaroniFacit(value);
            redirectAttributes.addFlashAttribute("success", "Makaronifacit sparat.");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Ogiltigt facit — ange ett tal (antal makaroner).");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam String username,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        try {
            String removed = userService.deleteUser(username, principal.getUsername());
            redirectAttributes.addFlashAttribute("success", "Deltagaren \"" + removed + "\" är borttagen.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/admin")
    public String setAdmin(@RequestParam String username,
                           @RequestParam boolean makeAdmin,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.setAdmin(username, makeAdmin, principal.getUsername());
            redirectAttributes.addFlashAttribute("success", makeAdmin
                    ? "\"" + username + "\" är nu admin — hen behöver logga ut och in igen för att få tillgång."
                    : "\"" + username + "\" är inte längre admin.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
