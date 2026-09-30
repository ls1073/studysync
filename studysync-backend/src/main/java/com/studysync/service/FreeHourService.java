package com.studysync.service;

import com.studysync.entity.AcademicCalendarEntry;
import com.studysync.entity.TimetableSlot;
import com.studysync.entity.User;
import com.studysync.repository.AcademicCalendarEntryRepository;
import com.studysync.repository.DayOrderTemplateRepository;
import com.studysync.repository.TimetableSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Core engine: combines a student's Day-Order Timetable Template with their
 * Academic Calendar Mapping to compute genuinely free time slots for any date.
 *
 * Models a realistic daily routine on top of raw class time:
 *  - Wake time each day, pulled from that day's own first class start (minus
 *    travel + prep time), clamped to a sane range. Holidays use a fixed
 *    default wake time.
 *  - Bedtime each night, pulled from TOMORROW's wake time minus a target
 *    sleep duration (6-6.5h), clamped to a sane range.
 *  - Morning prep + breakfast (getting ready for college) immediately after
 *    waking, only on college days.
 *  - Commute time computed from the student's actual travel distance (falls
 *    back to a default if not set) plus a fixed traffic/buffer margin, placed
 *    immediately before the first class and after the last class.
 *  - A short rest/eating block right after arriving home from college.
 *  - Fixed lunch and dinner windows every day (college day or holiday).
 * All of the college-linked buffers (prep, commute, post-commute rest) are
 * skipped entirely on holidays -- only meals remain.
 */
@Service
@RequiredArgsConstructor
public class FreeHourService {

    private final AcademicCalendarEntryRepository calendarEntryRepository;
    private final DayOrderTemplateRepository dayOrderTemplateRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    @Value("${app.scheduling.default-wake-time}")
    private String defaultWakeTimeStr;

    @Value("${app.scheduling.earliest-wake-time}")
    private String earliestWakeTimeStr;

    @Value("${app.scheduling.latest-wake-time}")
    private String latestWakeTimeStr;

    @Value("${app.scheduling.earliest-bedtime}")
    private String earliestBedtimeStr;

    @Value("${app.scheduling.latest-bedtime}")
    private String latestBedtimeStr;

    @Value("${app.scheduling.sleep-duration-hours}")
    private double sleepDurationHours;

    @Value("${app.scheduling.travel-speed-kmh}")
    private double travelSpeedKmh;

    @Value("${app.scheduling.default-travel-minutes}")
    private int defaultTravelMinutes;

    @Value("${app.scheduling.travel-buffer-extra-minutes}")
    private int travelBufferExtraMinutes;

    @Value("${app.scheduling.morning-prep-minutes}")
    private int morningPrepMinutes;

    @Value("${app.scheduling.post-commute-rest-minutes}")
    private int postCommuteRestMinutes;

    @Value("${app.scheduling.lunch-start}")
    private String lunchStartStr;

    @Value("${app.scheduling.lunch-minutes}")
    private int lunchMinutes;

    @Value("${app.scheduling.dinner-start}")
    private String dinnerStartStr;

    @Value("${app.scheduling.dinner-minutes}")
    private int dinnerMinutes;

    public List<FreeSlot> computeFreeSlots(User user, LocalDate date) {
        LocalTime wakeTime = computeWakeTime(user, date);
        LocalTime bedTime = computeBedTime(user, date);

        if (!bedTime.isAfter(wakeTime)) {
            return new ArrayList<>();
        }

        Optional<AcademicCalendarEntry> entryOpt = calendarEntryRepository.findByUserAndDate(user, date);
        if (entryOpt.isEmpty()) {
            return new ArrayList<>();
        }
        AcademicCalendarEntry entry = entryOpt.get();
        boolean isHoliday = Boolean.TRUE.equals(entry.getIsHoliday());

        List<TimetableSlot> classSlots = new ArrayList<>();
        if (!isHoliday) {
            if (entry.getDayOrderNumber() == null) {
                return new ArrayList<>();
            }
            classSlots = dayOrderTemplateRepository
                    .findByUserAndDayOrderNumber(user, entry.getDayOrderNumber())
                    .map(timetableSlotRepository::findByDayOrderTemplateOrderByStartTimeAsc)
                    .orElse(new ArrayList<>());
            classSlots.sort(Comparator.comparing(TimetableSlot::getStartTime));
        }

        List<BusyInterval> busyIntervals = new ArrayList<>();
        for (TimetableSlot s : classSlots) {
            busyIntervals.add(new BusyInterval(s.getStartTime(), s.getEndTime()));
        }

        if (!classSlots.isEmpty()) {
            int travelMinutes = computeTravelMinutes(user);
            LocalTime firstStart = classSlots.get(0).getStartTime();
            LocalTime lastEnd = classSlots.get(classSlots.size() - 1).getEndTime();

            // Morning prep (bath, getting ready, breakfast) right after waking,
            // then travel to college immediately before the first class.
            LocalTime prepEnd = addMinutesSafe(wakeTime, morningPrepMinutes, bedTime);
            busyIntervals.add(new BusyInterval(wakeTime, prepEnd));

            LocalTime travelToCollegeStart = subtractMinutesSafe(firstStart, travelMinutes, prepEnd);
            busyIntervals.add(new BusyInterval(travelToCollegeStart, firstStart));

            // Travel home right after the last class, then a short rest/eating block.
            LocalTime travelHomeEnd = addMinutesSafe(lastEnd, travelMinutes, bedTime);
            busyIntervals.add(new BusyInterval(lastEnd, travelHomeEnd));

            LocalTime postCommuteRestEnd = addMinutesSafe(travelHomeEnd, postCommuteRestMinutes, bedTime);
            busyIntervals.add(new BusyInterval(travelHomeEnd, postCommuteRestEnd));
        }

        addMealBuffers(busyIntervals, wakeTime, bedTime);

        return subtractBusyFromDay(wakeTime, bedTime, busyIntervals);
    }

    public double computeTotalFreeHours(User user, LocalDate date) {
        return computeFreeSlots(user, date).stream().mapToDouble(FreeSlot::hours).sum();
    }

    public boolean isHoliday(User user, LocalDate date) {
        return calendarEntryRepository.findByUserAndDate(user, date)
                .map(AcademicCalendarEntry::getIsHoliday)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    /**
     * Converts the student's actual commute distance into a travel time, using an
     * assumed average speed, plus a fixed extra margin for traffic/rest at each end.
     * Falls back to a default distance-based time if the student hasn't set a
     * distance yet.
     */
    private int computeTravelMinutes(User user) {
        Double distanceKm = user.getCommuteDistanceKm();
        if (distanceKm == null || distanceKm <= 0) {
            return defaultTravelMinutes + travelBufferExtraMinutes;
        }
        int rawTravelMinutes = (int) Math.ceil((distanceKm / travelSpeedKmh) * 60.0);
        return rawTravelMinutes + travelBufferExtraMinutes;
    }

    private void addMealBuffers(List<BusyInterval> busyIntervals, LocalTime wakeTime, LocalTime bedTime) {
        LocalTime lunchStart = LocalTime.parse(lunchStartStr);
        if (lunchStart.isAfter(wakeTime) && lunchStart.isBefore(bedTime)) {
            LocalTime lunchEnd = addMinutesSafe(lunchStart, lunchMinutes, bedTime);
            busyIntervals.add(new BusyInterval(lunchStart, lunchEnd));
        }

        LocalTime dinnerStart = LocalTime.parse(dinnerStartStr);
        if (dinnerStart.isAfter(wakeTime) && dinnerStart.isBefore(bedTime)) {
            LocalTime dinnerEnd = addMinutesSafe(dinnerStart, dinnerMinutes, bedTime);
            busyIntervals.add(new BusyInterval(dinnerStart, dinnerEnd));
        }
    }

    private LocalTime computeWakeTime(User user, LocalDate date) {
        LocalTime firstClass = firstClassStartOrNull(user, date);
        LocalTime earliest = LocalTime.parse(earliestWakeTimeStr);
        LocalTime latest = LocalTime.parse(latestWakeTimeStr);

        if (firstClass == null) {
            LocalTime def = LocalTime.parse(defaultWakeTimeStr);
            if (def.isBefore(earliest)) return earliest;
            if (def.isAfter(latest)) return latest;
            return def;
        }

        int travelMinutes = computeTravelMinutes(user);
        LocalTime target = subtractMinutesSafe(firstClass, travelMinutes + morningPrepMinutes, earliest);
        if (target.isBefore(earliest)) return earliest;
        if (target.isAfter(latest)) return latest;
        return target;
    }

    private LocalTime computeBedTime(User user, LocalDate date) {
        LocalTime nextWake = computeWakeTime(user, date.plusDays(1));
        LocalTime earliestBed = LocalTime.parse(earliestBedtimeStr);
        LocalTime latestBed = LocalTime.parse(latestBedtimeStr);

        long sleepMinutes = Math.round(sleepDurationHours * 60);
        LocalTime target = nextWake.minusMinutes(sleepMinutes);

        if (target.isAfter(LocalTime.NOON) && nextWake.isBefore(LocalTime.NOON)) {
            return latestBed;
        }
        if (target.isBefore(earliestBed)) return earliestBed;
        if (target.isAfter(latestBed)) return latestBed;
        return target;
    }

    private LocalTime firstClassStartOrNull(User user, LocalDate date) {
        Optional<AcademicCalendarEntry> entryOpt = calendarEntryRepository.findByUserAndDate(user, date);
        if (entryOpt.isEmpty() || Boolean.TRUE.equals(entryOpt.get().getIsHoliday()) || entryOpt.get().getDayOrderNumber() == null) {
            return null;
        }
        List<TimetableSlot> slots = dayOrderTemplateRepository
                .findByUserAndDayOrderNumber(user, entryOpt.get().getDayOrderNumber())
                .map(timetableSlotRepository::findByDayOrderTemplateOrderByStartTimeAsc)
                .orElse(new ArrayList<>());
        if (slots.isEmpty()) return null;
        return slots.stream().min(Comparator.comparing(TimetableSlot::getStartTime)).get().getStartTime();
    }

    private LocalTime subtractMinutesSafe(LocalTime time, int minutes, LocalTime floor) {
        LocalTime result = time.minusMinutes(minutes);
        if (result.isAfter(time)) return floor;
        return result.isBefore(floor) ? floor : result;
    }

    private LocalTime addMinutesSafe(LocalTime time, int minutes, LocalTime ceiling) {
        LocalTime result = time.plusMinutes(minutes);
        if (result.isBefore(time)) return ceiling;
        return result.isAfter(ceiling) ? ceiling : result;
    }

    private List<FreeSlot> subtractBusyFromDay(LocalTime dayStart, LocalTime dayEnd, List<BusyInterval> busyIntervals) {
        List<FreeSlot> free = new ArrayList<>();

        List<BusyInterval> sorted = new ArrayList<>(busyIntervals);
        sorted.sort(Comparator.comparing(b -> b.start));

        LocalTime cursor = dayStart;

        for (BusyInterval b : sorted) {
            LocalTime busyStart = b.start.isBefore(dayStart) ? dayStart : b.start;
            LocalTime busyEnd = b.end.isAfter(dayEnd) ? dayEnd : b.end;

            if (busyStart.isAfter(cursor)) {
                free.add(new FreeSlot(cursor, busyStart));
            }
            if (busyEnd.isAfter(cursor)) {
                cursor = busyEnd;
            }
        }

        if (cursor.isBefore(dayEnd)) {
            free.add(new FreeSlot(cursor, dayEnd));
        }

        return free;
    }

    private static class BusyInterval {
        final LocalTime start;
        final LocalTime end;
        BusyInterval(LocalTime start, LocalTime end) { this.start = start; this.end = end; }
    }
}
