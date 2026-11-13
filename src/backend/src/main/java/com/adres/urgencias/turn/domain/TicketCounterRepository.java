package com.adres.urgencias.turn.domain;

import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.LocalDate;

/** Storage of the daily ticket counters (table {@code ticket_counter}, CON-TRG-02). */
public interface TicketCounterRepository {

    /**
     * Atomically advances the counter of the level for the day and returns the new value; the
     * first call of a day returns 1. Must run inside the transaction that inserts the ticket, so
     * that a rolled-back assignment does not consume a number.
     */
    int nextNumber(LocalDate date, TriageLevel level);
}
