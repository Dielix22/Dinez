package com.adres.urgencias.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TicketTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 12);
    private static final LocalDateTime ARRIVAL = LocalDateTime.of(2026, 11, 12, 10, 0);

    @Test
    void shouldCreateWaitingTicketAndUpdatePositionAndLevel() {
        Ticket ticket = new Ticket(10L, DATE, "T3-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_3, 1, null);

        assertTrue(ticket.isWaiting());
        assertEquals(10L, ticket.getTicketId().orElseThrow());
        assertTrue(ticket.getExitTime().isEmpty());

        ticket.assignPosition(3);
        assertEquals(3, ticket.getPosition());

        ticket.changeLevel(TriageLevel.LEVEL_2, 1);
        assertEquals(TriageLevel.LEVEL_2, ticket.getLevel());
        assertEquals(1, ticket.getPosition());
    }

    @Test
    void shouldEvaluateWaitLimitExpirationStrictlyGreaterThanLimit() {
        Ticket ticket = new Ticket(null, DATE, "T2-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_2, 1, null);
        Duration limit = Duration.ofMinutes(30);

        assertFalse(ticket.hasExceededLimit(limit, ARRIVAL.plusMinutes(30)));
        assertTrue(ticket.hasExceededLimit(limit, ARRIVAL.plusMinutes(31)));
    }

    @Test
    void shouldRetireTicketAndPreventFurtherModifications() {
        Ticket ticket = new Ticket(null, DATE, "T4-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_4, 1, null);
        LocalDateTime exit = ARRIVAL.plusMinutes(25);

        ticket.retire(exit);

        assertFalse(ticket.isWaiting());
        assertEquals(exit, ticket.getExitTime().orElseThrow());
        assertThrows(InvalidDataException.class, () -> ticket.retire(exit.plusMinutes(1)));
        assertThrows(InvalidDataException.class, () -> ticket.assignPosition(2));
        assertThrows(InvalidDataException.class, () -> ticket.changeLevel(TriageLevel.LEVEL_3, 1));
    }

    @Test
    void shouldRejectInvalidConstructorInvariants() {
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(null, null, "T3-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_3, 1, null));
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(null, DATE, " ", "PAT-001", ARRIVAL, TriageLevel.LEVEL_3, 1, null));
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(null, DATE, "T3-001", "", ARRIVAL, TriageLevel.LEVEL_3, 1, null));
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(null, DATE, "T1-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_1, 1, null));
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(null, DATE, "T3-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_3, 0, null));
        assertThrows(
                InvalidDataException.class,
                () -> new Ticket(
                        null, DATE, "T3-001", "PAT-001", ARRIVAL, TriageLevel.LEVEL_3, 1, ARRIVAL.minusMinutes(1)));
    }
}