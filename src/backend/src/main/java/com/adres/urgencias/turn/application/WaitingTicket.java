package com.adres.urgencias.turn.application;

import com.adres.urgencias.shared.domain.Ticket;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * A waiting ticket as the API exposes it. The domain keeps the arrival time as hospital local time;
 * here it becomes an instant so that clients in any time zone read it correctly. The ticket number
 * restarts every day, so {@code ticketDate} is part of the identity of a ticket: a patient still
 * waiting after midnight can hold the same number as a patient of the new day.
 */
public record WaitingTicket(
        LocalDate ticketDate,
        String ticketNumber,
        String patientId,
        int level,
        int position,
        Instant arrivalTime) {

    static WaitingTicket from(Ticket ticket, ZoneId hospitalZone) {
        return new WaitingTicket(
                ticket.getTicketDate(),
                ticket.getTicketNumber(),
                ticket.getPatientId(),
                ticket.getLevel().getLevel(),
                ticket.getPosition(),
                ticket.getArrivalTime().atZone(hospitalZone).toInstant());
    }
}
