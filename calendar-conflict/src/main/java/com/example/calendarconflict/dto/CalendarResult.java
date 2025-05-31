package com.example.calendarconflict.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarResult {
    private List<Conflict> conflicts;
    private List<Slot> freeSlots;

}
