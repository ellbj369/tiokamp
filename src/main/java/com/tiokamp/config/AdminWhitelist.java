package com.tiokamp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The permanent admins from configuration (app.admin-usernames / the
 * APP_ADMIN_USERNAMES env var). These are always admins and can't be demoted
 * from the UI — a safety net so there is always a way in. Everyone else is
 * promoted/demoted dynamically via the admin page (the User.admin flag).
 */
@Component
public class AdminWhitelist {

    private final Set<String> usernames;

    public AdminWhitelist(@Value("${app.admin-usernames:}") String csv) {
        usernames = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isListed(String username) {
        return username != null && usernames.contains(username.toLowerCase(Locale.ROOT));
    }
}
