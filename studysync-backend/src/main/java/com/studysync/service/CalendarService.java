package com.studysync.service;

import com.studysync.dto.CalendarDtos.*;
import com.studysync.entity.AcademicCalendarEntry;
import com.studysync.entity.User;
import com.studysync.repository.AcademicCalendarEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final AcademicCalendarEntryRepository calendarEntryRepository;

    @Transactional
    public EntryResponse setSingleEntry(User user, SingleEntryRequest request) {
        AcademicCalendarEntry entry = calendarEntryRepository.findByUserAndDate(user, request.getDate())
                .orElseGet(() -> AcademicCalendarEntry.builder().user(user).date(request.getDate()).build());

        boolean holiday = Boolean.TRUE.equals(request.getIsHoliday());
        entry.setIsHoliday(holiday);
        entry.setDayOrderNumber(holiday ? null : request.getDayOrderNumber());

        entry = calendarEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional
    public List<EntryResponse> setBulkRange(User user, BulkRangeRequest request) {
        List<EntryResponse> results = new ArrayList<>();
        Set<LocalDate> excluded = new HashSet<>();
        if (request.getExcludeDates() != null) {
            excluded.addAll(request.getExcludeDates());
        }

        List<Integer> cycle = request.getCyclePattern();
        int cycleLength = cycle.size();
        int cycleIndex = 0;

        LocalDate current = request.getStartDate();
        while (!current.isAfter(request.getEndDate())) {
            final LocalDate loopDate = current;
            AcademicCalendarEntry entry = calendarEntryRepository.findByUserAndDate(user, loopDate)
                    .orElseGet(() -> AcademicCalendarEntry.builder().user(user).date(loopDate).build());

            if (excluded.contains(current)) {
                entry.setIsHoliday(true);
                entry.setDayOrderNumber(null);
            } else {
                entry.setIsHoliday(false);
                entry.setDayOrderNumber(cycle.get(cycleIndex % cycleLength));
                cycleIndex++;
            }

            results.add(toResponse(calendarEntryRepository.save(entry)));
            current = current.plusDays(1);
        }

        return results;
    }

    public List<EntryResponse> getRange(User user, LocalDate start, LocalDate end) {
        List<EntryResponse> results = new ArrayList<>();
        for (AcademicCalendarEntry e : calendarEntryRepository.findByUserAndDateBetweenOrderByDateAsc(user, start, end)) {
            results.add(toResponse(e));
        }
        return results;
    }

    private EntryResponse toResponse(AcademicCalendarEntry e) {
        return new EntryResponse(e.getId(), e.getDate(), e.getDayOrderNumber(), e.getIsHoliday());
    }
}
