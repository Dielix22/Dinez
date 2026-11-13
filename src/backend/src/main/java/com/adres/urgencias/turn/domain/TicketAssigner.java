package com.adres.urgencias.turn.domain;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Generates sequential daily ticket numbers per triage level and delegates queueing to PriorityQueueManager (REQ-04, CON-TRG-02).
 */
public class TicketAssigner {

    private final PriorityQueueManager priorityQueueManager;
    private final Map<TriageLevel, Integer> dailyCounters;
    private final Clock clock;
    private LocalDate currentCounterDate;

    public TicketAssigner(PriorityQueueManager priorityQueueManager, Clock clock) {
        if (priorityQueueManager == null || clock == null) {
            throw new InvalidDataException("PriorityQueueManager and clock must not be null");
        }
        this.priorityQueueManager = priorityQueueManager;
        this.clock = clock;
        this.dailyCounters = new EnumMap<>(TriageLevel.class);
        this.currentCounterDate = LocalDate.now(clock);
    }

    /**
     * Assigns a new ticket number (e.g. T3-015) and places the patient in the priority queue (REQ-04).
     */
    public Ticket assignTicket(String patientId, TriageLevel level) {
        if (patientId == null || patientId.isBlank()) {
            throw new InvalidDataException("Patient ID must not be blank");
        }
        if (level == null || !level.hasQueue()) {
            throw new InvalidDataException("Triage level must be between LEVEL_2 and LEVEL_5");
        }
        if (priorityQueueManager.patientExists(patientId)) {
            throw new DuplicatePatientException(patientId);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        resetCountersIfNewDay(today);

        String ticketNumber = generateNextTicketNumber(level);
        Ticket ticket = new Ticket(null, today, ticketNumber, patientId, now, level, 1, null);

        priorityQueueManager.addPatient(ticket);
        return ticket;
    }

    private void resetCountersIfNewDay(LocalDate today) {
        if (!today.equals(currentCounterDate)) {
            dailyCounters.clear();
            currentCounterDate = today;
        }
    }

    private String generateNextTicketNumber(TriageLevel level) {
        int nextNumber = dailyCounters.getOrDefault(level, 0) + 1;
        dailyCounters.put(level, nextNumber);
        return String.format("T%d-%03d", level.getLevel(), nextNumber);
    }
}