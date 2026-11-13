package com.adres.urgencias.turn.application;

/**
 * Outcome of processing a triage result: the patient waits with a ticket, or is level 1 and goes
 * to immediate care without a ticket ({@code ticket} is then null).
 */
public record TicketAssignmentResult(String patientId, int level, WaitingTicket ticket) {

    public boolean isImmediateCare() {
        return ticket == null;
    }
}
