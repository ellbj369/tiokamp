package com.tiokamp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopEntryDto {
    private int rank;
    private String username;
    private String profilePicture;
    private String value; // pre-formatted with Swedish decimal comma
}
