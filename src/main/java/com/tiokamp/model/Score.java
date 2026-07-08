package com.tiokamp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
@Data
@NoArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── The 10 kamp events ───────────────────────────────
    private Double event1;   // 100m sprint
    private Double event2;   // Long jump
    private Double event3;   // Shot put
    private Double event4;   // High jump
    private Double event5;   // 400m
    private Double event6;   // 110m hurdles
    private Double event7;   // Discus
    private Double event8;   // Pole vault
    private Double event9;   // Javelin
    private Double event10;  // 1500m

    private Double totalScore = 0.0;

    // Tracks which event was most recently updated (for the leaderboard banner)
    private String lastUpdatedEvent;
    private Double lastUpdatedValue;
    private LocalDateTime lastUpdatedAt;

    public void setEventByNumber(int eventNum, Double value) {
        switch (eventNum) {
            case 1  -> event1  = value;
            case 2  -> event2  = value;
            case 3  -> event3  = value;
            case 4  -> event4  = value;
            case 5  -> event5  = value;
            case 6  -> event6  = value;
            case 7  -> event7  = value;
            case 8  -> event8  = value;
            case 9  -> event9  = value;
            case 10 -> event10 = value;
        }
        this.lastUpdatedEvent = eventLabel(eventNum);
        this.lastUpdatedValue = value;
        this.lastUpdatedAt    = LocalDateTime.now();
        recalculateTotal();
    }

    public void recalculateTotal() {
        totalScore = 0.0;
        if (event1  != null) totalScore += event1;
        if (event2  != null) totalScore += event2;
        if (event3  != null) totalScore += event3;
        if (event4  != null) totalScore += event4;
        if (event5  != null) totalScore += event5;
        if (event6  != null) totalScore += event6;
        if (event7  != null) totalScore += event7;
        if (event8  != null) totalScore += event8;
        if (event9  != null) totalScore += event9;
        if (event10 != null) totalScore += event10;
    }

    private String eventLabel(int n) {
        return switch (n) {
            case 1  -> "100m Sprint";
            case 2  -> "Long Jump";
            case 3  -> "Shot Put";
            case 4  -> "High Jump";
            case 5  -> "400m";
            case 6  -> "110m Hurdles";
            case 7  -> "Discus";
            case 8  -> "Pole Vault";
            case 9  -> "Javelin";
            case 10 -> "1500m";
            default -> "Event " + n;
        };
    }
}
