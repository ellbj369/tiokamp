package com.tiokamp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EventCategoryDto {
    private String eventName;
    private List<TopEntryDto> top3;
    private String emptyMessage; // shown when top3 is empty
}
