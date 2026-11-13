package com.adres.urgencias.queue.domain;

import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.util.List;

/**
 * Storage of the tickets (table {@code ticket}). The waiting tickets of a level, ordered by
 * position, are its queue (REQ-04, BR-TRG-02).
 */
public interface TicketRepository {

    /** Tells whether the patient has a ticket that has not been retired yet. */
    boolean existsWaitingForPatient(String patientId);

    /** Position a new ticket takes at the end of the level: highest waiting position plus one. */
    int nextPosition(TriageLevel level);

    /**
     * Inserts the ticket and returns it with its generated id.
     *
     * @throws com.adres.urgencias.shared.domain.DuplicatePatientException if the patient already
     *     has a waiting ticket (concurrent request)
     */
    Ticket save(Ticket ticket);

    /** Waiting tickets: most urgent level first, then by position. */
    List<Ticket> findWaitingOrdered();
}
