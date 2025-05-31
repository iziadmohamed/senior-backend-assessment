package com.example.calendarconflict.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Conflict {
    private String event1;
    private String event2;
    private ZonedDateTime overlapStart;
    private ZonedDateTime overlapEnd;
}
