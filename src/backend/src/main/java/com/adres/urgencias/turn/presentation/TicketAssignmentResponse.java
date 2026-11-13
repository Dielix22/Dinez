package com.adres.urgencias.turn.presentation;

import com.adres.urgencias.turn.application.TicketAssignmentResult;
import com.adres.urgencias.turn.application.WaitingTicket;
import java.time.Instant;

/**
 * Answer of {@code POST /api/tickets}. When {@code immediateCare} is true (level 1) there is no
 * ticket and {@code ticketNumber}, {@code arrivalTime} and {@code position} are null.
 */
public record TicketAssignmentResponse(
        String patientId,
        int level,
        boolean immediateCare,
        String ticketNumber,
        Instant arrivalTime,
        Integer position) {

    static TicketAssignmentResponse from(TicketAssignmentResult result) {
        if (result.isImmediateCare()) {
            return new TicketAssignmentResponse(
                    result.patientId(), result.level(), true, null, null, null);
        }
        WaitingTicket ticket = result.ticket();
        return new TicketAssignmentResponse(
                result.patientId(),
                result.level(),
                false,
                ticket.ticketNumber(),
                ticket.arrivalTime(),
                ticket.position());
    }
}
