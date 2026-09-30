package com.studysync.service;

import com.studysync.dto.TimetableDtos.*;
import com.studysync.entity.DayOrderTemplate;
import com.studysync.entity.TimetableSlot;
import com.studysync.entity.User;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.repository.DayOrderTemplateRepository;
import com.studysync.repository.TimetableSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final DayOrderTemplateRepository dayOrderTemplateRepository;
    private final TimetableSlotRepository timetableSlotRepository;

    @Transactional
    public DayOrderResponse createOrUpdateDayOrder(User user, DayOrderRequest request) {
        DayOrderTemplate template = dayOrderTemplateRepository
                .findByUserAndDayOrderNumber(user, request.getDayOrderNumber())
                .orElseGet(() -> DayOrderTemplate.builder()
                        .user(user)
                        .dayOrderNumber(request.getDayOrderNumber())
                        .build());

        template.setLabel(request.getLabel());
        template = dayOrderTemplateRepository.save(template);

        // replace all slots for simplicity/predictability
        timetableSlotRepository.deleteByDayOrderTemplate(template);
        List<TimetableSlot> slots = new ArrayList<>();
        if (request.getSlots() != null) {
            for (SlotRequest sr : request.getSlots()) {
                slots.add(TimetableSlot.builder()
                        .dayOrderTemplate(template)
                        .subjectName(sr.getSubjectName())
                        .startTime(sr.getStartTime())
                        .endTime(sr.getEndTime())
                        .build());
            }
            timetableSlotRepository.saveAll(slots);
        }

        return toResponse(template, slots);
    }

    public List<DayOrderResponse> getAllDayOrders(User user) {
        List<DayOrderTemplate> templates = dayOrderTemplateRepository.findByUserOrderByDayOrderNumberAsc(user);
        List<DayOrderResponse> result = new ArrayList<>();
        for (DayOrderTemplate t : templates) {
            List<TimetableSlot> slots = timetableSlotRepository.findByDayOrderTemplateOrderByStartTimeAsc(t);
            result.add(toResponse(t, slots));
        }
        return result;
    }

    public void deleteDayOrder(User user, Long dayOrderTemplateId) {
        DayOrderTemplate template = dayOrderTemplateRepository.findById(dayOrderTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Day order not found"));
        if (!template.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Day order not found");
        }
        dayOrderTemplateRepository.delete(template);
    }

    List<TimetableSlot> getSlotsForDayOrder(User user, Integer dayOrderNumber) {
        return dayOrderTemplateRepository.findByUserAndDayOrderNumber(user, dayOrderNumber)
                .map(timetableSlotRepository::findByDayOrderTemplateOrderByStartTimeAsc)
                .orElse(new ArrayList<>());
    }

    private DayOrderResponse toResponse(DayOrderTemplate template, List<TimetableSlot> slots) {
        List<SlotResponse> slotResponses = new ArrayList<>();
        for (TimetableSlot s : slots) {
            slotResponses.add(new SlotResponse(s.getId(), s.getSubjectName(), s.getStartTime(), s.getEndTime()));
        }
        return new DayOrderResponse(template.getId(), template.getDayOrderNumber(), template.getLabel(), slotResponses);
    }
}
