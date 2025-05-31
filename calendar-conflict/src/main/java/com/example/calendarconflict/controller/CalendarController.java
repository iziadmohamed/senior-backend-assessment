package com.example.calendarconflict.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.calendarconflict.dto.CalendarInput;
import com.example.calendarconflict.dto.CalendarResult;
import com.example.calendarconflict.service.CalendarService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    
    private final CalendarService optimizerService;


    @PostMapping("/analyze")
    public CalendarResult analyze(@RequestBody CalendarInput input) {
        return optimizerService.analyze(input);
    }
}
