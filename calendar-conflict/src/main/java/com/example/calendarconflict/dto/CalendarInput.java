package com.example.calendarconflict.dto;

import java.util.List;
import lombok.Data;

@Data
public class CalendarInput {
    private WorkingHours workingHours;
    private String timeZone;
    private List<Event> events;

}