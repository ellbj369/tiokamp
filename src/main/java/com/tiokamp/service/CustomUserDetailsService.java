package com.tiokamp.service;

import com.tiokamp.config.AdminWhitelist;
import com.tiokamp.model.User;
import com.tiokamp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminWhitelist adminWhitelist;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Admin either permanently (config whitelist) or by dynamic grant (DB flag).
        boolean admin = user.isAdmin() || adminWhitelist.isListed(user.getUsername());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(admin ? new String[]{"USER", "ADMIN"} : new String[]{"USER"})
                .build();
    }
}
