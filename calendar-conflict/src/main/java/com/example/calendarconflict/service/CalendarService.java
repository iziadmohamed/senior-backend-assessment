package com.example.calendarconflict.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.calendarconflict.dto.CalendarInput;
import com.example.calendarconflict.dto.CalendarResult;
import com.example.calendarconflict.dto.Conflict;
import com.example.calendarconflict.dto.Event;
import com.example.calendarconflict.dto.Slot;
import com.example.calendarconflict.dto.WorkingHours;

@Service
public class CalendarService {
    
    public CalendarResult analyze(CalendarInput input) {
        ZoneId zone = ZoneId.of(input.getTimeZone());

        LocalTime workStart = LocalTime.parse(input.getWorkingHours().getStart());
        LocalTime workEnd = LocalTime.parse(input.getWorkingHours().getEnd());

        LocalDate date = input.getEvents().get(0).getStartTime().toLocalDate();
        ZonedDateTime workStartZdt = ZonedDateTime.of(date, workStart, zone);
        ZonedDateTime workEndZdt = ZonedDateTime.of(date, workEnd, zone);

        List<Event> sortedEvents = input.getEvents().stream()
                .sorted(Comparator.comparing(Event::getStartTime))
                .collect(Collectors.toList());

        List<Conflict> conflicts = new ArrayList<>();
        for (int i = 0; i < sortedEvents.size(); i++) {
            Event e1 = sortedEvents.get(i);
            for (int j = i + 1; j < sortedEvents.size(); j++) {
                Event e2 = sortedEvents.get(j);
                if (e1.getEndTime().isAfter(e2.getStartTime())) {
                    ZonedDateTime overlapStart = e2.getStartTime();
                    ZonedDateTime overlapEnd = e1.getEndTime().isBefore(e2.getEndTime()) ? e1.getEndTime() : e2.getEndTime();
                    conflicts.add(new Conflict(e1.getTitle(), e2.getTitle(), overlapStart, overlapEnd));
                } else {
                    break;
                }
            }
        }

        List<Slot> freeSlots = new ArrayList<>();
        ZonedDateTime cursor = workStartZdt;

        for (Event event : sortedEvents) {
            if (cursor.isBefore(event.getStartTime())) {
                ZonedDateTime freeStart = cursor;
                ZonedDateTime freeEnd = event.getStartTime();
                if (freeEnd.isAfter(workEndZdt)) freeEnd = workEndZdt;
                if (freeStart.isBefore(freeEnd))
                    freeSlots.add(new Slot(freeStart, freeEnd));
            }
            if (cursor.isBefore(event.getEndTime())) {
                cursor = event.getEndTime();
            }
        }

        if (cursor.isBefore(workEndZdt)) {
            freeSlots.add(new Slot(cursor, workEndZdt));
        }

        return new CalendarResult(conflicts, freeSlots);
    }
    
}
