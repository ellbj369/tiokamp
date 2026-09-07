package com.tiokamp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LatestScoreDto {
    private String username;
    private String profilePicture;
    private String eventName;
    private String eventValue; // pre-formatted with Swedish decimal comma
    private Double totalScore;
    private String updatedAt; // formatted time string
}
