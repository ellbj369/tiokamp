package com.tiokamp.controller;

import com.tiokamp.model.User;
import com.tiokamp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    // Most guests arrive once, create an account and are done — so registration
    // is the front door; login is linked from there for the rare returner.
    @GetMapping("/")
    public String root() {
        return "redirect:/register";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", "Fel användarnamn eller lösenord — försök igen.");
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
            HttpServletRequest request,
            HttpServletResponse response,
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
        User user;
        try {
            user = userService.register(username, password, profilePicture);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            log.error("Registration failed for username '{}'", username, e);
            redirectAttributes.addFlashAttribute("error", "Något gick fel när kontot skulle skapas — försök igen.");
            return "redirect:/register";
        }

        // Log the new user in directly so they land in the game
        try {
            Authentication auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
            redirectAttributes.addFlashAttribute("success",
                    "Välkommen " + user.getUsername() + "! Kontot är skapat — nu kör vi.");
            return "redirect:/scores";
        } catch (Exception e) {
            log.warn("Auto-login after registration failed for '{}'", user.getUsername(), e);
            redirectAttributes.addFlashAttribute("success", "Kontot är skapat! Snyggt jobbat. Nästa uppdrag: logga in.");
            return "redirect:/login";
        }
    }
}
