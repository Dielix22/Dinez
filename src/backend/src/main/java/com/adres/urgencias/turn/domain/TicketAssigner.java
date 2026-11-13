package com.adres.urgencias.turn.domain;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Generates sequential daily ticket numbers per triage level and delegates queueing to
 * PriorityQueueManager (REQ-04, CON-TRG-02, BR-TRG-04). It keeps no state: the counters are in
 * storage, keyed by day and level, so the numbering restarts each day by itself. Call it inside the
 * transaction that stores the patient, so a rejected assignment does not consume a number.
 */
public class TicketAssigner {

    private final PriorityQueueManager priorityQueueManager;
    private final TicketCounterRepository counters;
    private final Clock clock;

    public TicketAssigner(
            PriorityQueueManager priorityQueueManager,
            TicketCounterRepository counters,
            Clock clock) {
        if (priorityQueueManager == null || counters == null || clock == null) {
            throw new InvalidDataException(
                    "PriorityQueueManager, counters and clock must not be null");
        }
        this.priorityQueueManager = priorityQueueManager;
        this.counters = counters;
        this.clock = clock;
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

        String ticketNumber = generateNextTicketNumber(today, level);
        Ticket ticket = new Ticket(null, today, ticketNumber, patientId, now, level, 1, null);

        return priorityQueueManager.addPatient(ticket);
    }

    private String generateNextTicketNumber(LocalDate today, TriageLevel level) {
        int nextNumber = counters.nextNumber(today, level);
        return String.format("T%d-%03d", level.getLevel(), nextNumber);
    }
}
