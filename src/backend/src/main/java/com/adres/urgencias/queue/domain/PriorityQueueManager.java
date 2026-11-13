package com.adres.urgencias.queue.domain;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.util.List;

/**
 * Facade over the per-level priority queues and queue positions (REQ-04, REQ-05, REQ-11,
 * BR-TRG-02). It keeps no state: the queue of a level is the set of its waiting tickets in
 * storage, ordered by position.
 */
public class PriorityQueueManager {

    private final TicketRepository tickets;
    private final WaitTimeManager waitTimeManager;

    public PriorityQueueManager(TicketRepository tickets, WaitTimeManager waitTimeManager) {
        if (tickets == null || waitTimeManager == null) {
            throw new InvalidDataException("TicketRepository and WaitTimeManager must not be null");
        }
        this.tickets = tickets;
        this.waitTimeManager = waitTimeManager;
    }

    /**
     * Verifies the patient is not already waiting and stores the ticket at the end of its level
     * (REQ-04, BR-TRG-02).
     *
     * @return the stored ticket, with its id
     */
    public Ticket addPatient(Ticket ticket) {
        if (ticket == null) {
            throw new InvalidDataException("Ticket must not be null");
        }
        if (patientExists(ticket.getPatientId())) {
            throw new DuplicatePatientException(ticket.getPatientId());
        }
        ticket.assignPosition(tickets.nextPosition(ticket.getLevel()));
        return tickets.save(ticket);
    }

    public boolean patientExists(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return false;
        }
        return tickets.existsWaitingForPatient(patientId);
    }

    /** Waiting tickets: most urgent level first, then order of arrival. */
    public List<Ticket> getOrderedList() {
        return tickets.findWaitingOrdered();
    }

    public WaitTimeManager getWaitTimeManager() {
        return waitTimeManager;
    }
}
