package com.tiokamp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FinalStandingDto {
    private int rank;
    private String username;
    private String profilePicture;
    private String totalPoints; // pre-formatted with Swedish decimal comma
}
