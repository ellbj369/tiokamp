package com.tiokamp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminRowDto {
    private Integer rank;          // null until results are calculated
    private String username;
    private String profilePicture;
    private List<String> values;   // 10 pre-formatted raw values ("—" when missing)
    private List<String> points;   // 10 pre-formatted placement points, null until calculated
    private int filledCount;       // how many of the 10 events have a value
    private String totalPoints;    // null until calculated
    private boolean admin;         // currently an admin (config or dynamic)
    private boolean adminLocked;   // admin via config — can't be toggled in the UI
}
