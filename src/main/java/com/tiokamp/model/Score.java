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

    // ── The 10 kamp events (raw results) ─────────────────
    private Double event1;
    private Double event2;
    private Double event3;
    private Double event4;
    private Double event5;
    private Double event6;
    private Double event7;
    private Double event8;
    private Double event9;
    private Double event10;

    private Double totalScore = 0.0;

    // ── Final placement result (set once by the admin's calculation) ──
    private Double points1;
    private Double points2;
    private Double points3;
    private Double points4;
    private Double points5;
    private Double points6;
    private Double points7;
    private Double points8;
    private Double points9;
    private Double points10;
    private Double placementTotal;
    private Integer finalRank;

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
        this.lastUpdatedEvent = Event.byNumber(eventNum).getDisplayName();
        this.lastUpdatedValue = value;
        this.lastUpdatedAt    = LocalDateTime.now();
        recalculateTotal();
    }

    public Double getEventByNumber(int eventNum) {
        return switch (eventNum) {
            case 1  -> event1;
            case 2  -> event2;
            case 3  -> event3;
            case 4  -> event4;
            case 5  -> event5;
            case 6  -> event6;
            case 7  -> event7;
            case 8  -> event8;
            case 9  -> event9;
            case 10 -> event10;
            default -> null;
        };
    }

    public Double getPointsByNumber(int eventNum) {
        return switch (eventNum) {
            case 1  -> points1;
            case 2  -> points2;
            case 3  -> points3;
            case 4  -> points4;
            case 5  -> points5;
            case 6  -> points6;
            case 7  -> points7;
            case 8  -> points8;
            case 9  -> points9;
            case 10 -> points10;
            default -> null;
        };
    }

    public void applyFinalResult(double[] eventPoints, double totalPoints, int rank) {
        clearFinalResult();
        for (int i = 0; i < eventPoints.length && i < 10; i++) {
            setPointsByNumber(i + 1, eventPoints[i]);
        }
        placementTotal = totalPoints;
        finalRank = rank;
    }

    private void setPointsByNumber(int eventNum, Double value) {
        switch (eventNum) {
            case 1  -> points1  = value;
            case 2  -> points2  = value;
            case 3  -> points3  = value;
            case 4  -> points4  = value;
            case 5  -> points5  = value;
            case 6  -> points6  = value;
            case 7  -> points7  = value;
            case 8  -> points8  = value;
            case 9  -> points9  = value;
            case 10 -> points10 = value;
        }
    }

    public void clearFinalResult() {
        points1 = points2 = points3 = points4 = points5 = null;
        points6 = points7 = points8 = points9 = points10 = null;
        placementTotal = null;
        finalRank = null;
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

    public int getFilledCount() {
        int count = 0;
        for (int i = 1; i <= Event.count(); i++) {
            if (getEventByNumber(i) != null) count++;
        }
        return count;
    }
}
