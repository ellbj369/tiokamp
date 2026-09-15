package com.tiokamp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Filename of the stored profile picture (e.g. "abc123.jpg")
    private String profilePicture;

    // Dynamically granted admin (via the admin page). Nullable so the column can
    // be added to an existing database without a default; null means "not admin".
    @Column(name = "is_admin")
    private Boolean admin;

    /** True only when explicitly granted; treats a legacy null as not-admin. */
    public boolean isAdmin() {
        return Boolean.TRUE.equals(admin);
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Score score;

    public User(String username, String password, String profilePicture) {
        this.username = username;
        this.password = password;
        this.profilePicture = profilePicture;
    }
}
