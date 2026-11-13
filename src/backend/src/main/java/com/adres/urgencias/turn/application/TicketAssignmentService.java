package com.adres.urgencias.turn.application;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.domain.Patient;
import com.adres.urgencias.turn.domain.TicketAssigner;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * What the API does with a triage result while the Triage and Registration modules do not exist:
 * it stores the patient and then calls {@link TicketAssigner#assignTicket} (REQ-04).
 *
 * <p>The ticket rules themselves (number, queue, duplicate) are in the domain classes; this class
 * only orchestrates. Queues and counters are in the database, so one transaction covers the
 * patient, the counter and the ticket.
 */
@Service
public class TicketAssignmentService {

    private final PatientRepository patients;
    private final TicketAssigner ticketAssigner;
    private final PriorityQueueManager priorityQueueManager;
    private final Clock clock;

    public TicketAssignmentService(
            PatientRepository patients,
            TicketAssigner ticketAssigner,
            PriorityQueueManager priorityQueueManager,
            Clock clock) {
        this.patients = patients;
        this.ticketAssigner = ticketAssigner;
        this.priorityQueueManager = priorityQueueManager;
        this.clock = clock;
    }

    /**
     * Stores the patient and assigns a ticket. Level 1 gets no ticket (BR-TRG-03).
     *
     * <p>Everything runs in one transaction: if the assigner rejects the request, the patient
     * insert and the counter increment are rolled back and nothing is written.
     *
     * @throws InvalidDataException if a value is missing or the level is not between 1 and 5
     * @throws DuplicatePatientException if the patient already has a waiting ticket
     */
    @Transactional
    public TicketAssignmentResult assignTicket(
            String patientId, String document, String firstName, String lastName, Integer level) {
        TriageLevel triageLevel = toTriageLevel(level);
        Patient patient =
                new Patient(
                        trimmed(patientId),
                        trimmed(document),
                        trimmed(firstName),
                        trimmed(lastName),
                        triageLevel,
                        LocalDateTime.now(clock));

        // Checked before writing, so that a patient who is already waiting keeps the level of
        // the ticket even when the new level is 1 (changing level is REQ-05, not this operation).
        if (priorityQueueManager.patientExists(patient.getPatientId())) {
            throw new DuplicatePatientException(patient.getPatientId());
        }

        patients.save(patient);

        if (!triageLevel.hasQueue()) {
            return new TicketAssignmentResult(
                    patient.getPatientId(), triageLevel.getLevel(), null);
        }
        Ticket ticket = ticketAssigner.assignTicket(patient.getPatientId(), triageLevel);
        return new TicketAssignmentResult(
                patient.getPatientId(), triageLevel.getLevel(), toWaitingTicket(ticket));
    }

    /** Lists the waiting tickets: most urgent level first, then order of arrival. */
    @Transactional(readOnly = true)
    public List<WaitingTicket> listWaitingTickets() {
        return priorityQueueManager.getOrderedList().stream().map(this::toWaitingTicket).toList();
    }

    private WaitingTicket toWaitingTicket(Ticket ticket) {
        return WaitingTicket.from(ticket, clock.getZone());
    }

    private static TriageLevel toTriageLevel(Integer level) {
        if (level == null) {
            throw new InvalidDataException("Triage level is required");
        }
        return TriageLevel.fromValue(level);
    }

    private static String trimmed(String value) {
        return value == null ? null : value.trim();
    }
}
