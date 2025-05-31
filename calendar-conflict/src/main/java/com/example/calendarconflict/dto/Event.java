package com.example.calendarconflict.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private String title;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
   
}
