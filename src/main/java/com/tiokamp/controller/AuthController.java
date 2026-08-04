package com.tiokamp.controller;

import com.tiokamp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", "Fel username eller lösenord.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            RedirectAttributes redirectAttributes
    ) {
        if (username.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Du måste ha ett användarnamn och ett lösenord INGET FUSKERI HÄR.");
            return "redirect:/register";
        }
        if (profilePicture.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Du måste ha en bild!!! Ta hjälp av medtävlande om du är kameraskygg.");
            return "redirect:/register";
        }
        try {
            userService.register(username, password, profilePicture);
            redirectAttributes.addFlashAttribute("success", "Kontot skapat! Snyggt jobbat. Nästa uppdrag: logga in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
