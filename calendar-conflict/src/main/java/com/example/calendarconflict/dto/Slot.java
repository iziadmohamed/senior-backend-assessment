package com.example.calendarconflict.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Slot{
    private ZonedDateTime start;
    private ZonedDateTime end;

}
