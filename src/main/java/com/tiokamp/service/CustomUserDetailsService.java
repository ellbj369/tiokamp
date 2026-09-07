package com.tiokamp.service;

import com.tiokamp.model.User;
import com.tiokamp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Comma-separated usernames that get the ADMIN role (app.admin-usernames)
    @Value("${app.admin-usernames:}")
    private String adminUsernames;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(isAdmin(user.getUsername()) ? new String[]{"USER", "ADMIN"} : new String[]{"USER"})
                .build();
    }

    private boolean isAdmin(String username) {
        return Arrays.stream(adminUsernames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .anyMatch(name -> name.equalsIgnoreCase(username));
    }
}
