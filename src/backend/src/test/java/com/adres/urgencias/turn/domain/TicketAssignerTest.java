package com.adres.urgencias.turn.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketAssignerTest {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private MutableClock clock;
    private PriorityQueueManager priorityQueueManager;
    private TicketAssigner ticketAssigner;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(LocalDateTime.of(2026, 11, 12, 10, 0).atZone(ZONE).toInstant(), ZONE);
        WaitTimeManager waitTimeManager = new WaitTimeManager(Map.of(
                TriageLevel.LEVEL_2, Duration.ofMinutes(30),
                TriageLevel.LEVEL_3, Duration.ofMinutes(60),
                TriageLevel.LEVEL_4, Duration.ofMinutes(120),
                TriageLevel.LEVEL_5, Duration.ofMinutes(240)));
        priorityQueueManager = new PriorityQueueManager(waitTimeManager);
        ticketAssigner = new TicketAssigner(priorityQueueManager, clock);
    }

    @Test
    void shouldAssignSequentialTicketNumberAndEnqueueAtEndOfLevel() {
        for (int i = 1; i <= 14; i++) {
            ticketAssigner.assignTicket("PAT-PREV-" + i, TriageLevel.LEVEL_3);
        }

        Ticket fifteenth = ticketAssigner.assignTicket("PAT-015", TriageLevel.LEVEL_3);

        assertEquals("T3-015", fifteenth.getTicketNumber());
        assertEquals("PAT-015", fifteenth.getPatientId());
        assertEquals(TriageLevel.LEVEL_3, fifteenth.getLevel());
        assertEquals(15, fifteenth.getPosition());
        assertEquals(LocalDate.of(2026, 11, 12), fifteenth.getTicketDate());
        assertTrue(fifteenth.isWaiting());
        assertTrue(fifteenth.getExitTime().isEmpty());
    }

    @Test
    void shouldStartAt001ForFirstTicketOfLevelAndResetCountersOnNewDay() {
        Ticket firstDayTicket = ticketAssigner.assignTicket("PAT-401", TriageLevel.LEVEL_4);
        assertEquals("T4-001", firstDayTicket.getTicketNumber());
        assertEquals(1, firstDayTicket.getPosition());

        clock.advanceTo(LocalDateTime.of(2026, 11, 13, 0, 5).atZone(ZONE).toInstant());

        Ticket nextDayTicket = ticketAssigner.assignTicket("PAT-402", TriageLevel.LEVEL_4);
        assertEquals("T4-001", nextDayTicket.getTicketNumber());
        assertEquals(LocalDate.of(2026, 11, 13), nextDayTicket.getTicketDate());
    }

    @Test
    void shouldRejectDuplicatePatientWithoutIncrementingDailyCounter() {
        Ticket first = ticketAssigner.assignTicket("PAT-DUP", TriageLevel.LEVEL_3);
        assertEquals("T3-001", first.getTicketNumber());

        DuplicatePatientException exception = assertThrows(
                DuplicatePatientException.class, () -> ticketAssigner.assignTicket("PAT-DUP", TriageLevel.LEVEL_3));
        assertEquals("PACIENTE_DUPLICADO", exception.getErrorCode());

        Ticket nextValid = ticketAssigner.assignTicket("PAT-NEXT", TriageLevel.LEVEL_3);
        assertEquals("T3-002", nextValid.getTicketNumber());
        assertEquals(2, nextValid.getPosition());
    }

    @Test
    void shouldRejectBlankPatientIdNullLevelOrLevel1WithoutTouchingQueues() {
        InvalidDataException blankPatient = assertThrows(
                InvalidDataException.class, () -> ticketAssigner.assignTicket("   ", TriageLevel.LEVEL_2));
        InvalidDataException nullPatient = assertThrows(
                InvalidDataException.class, () -> ticketAssigner.assignTicket(null, TriageLevel.LEVEL_2));
        InvalidDataException nullLevel = assertThrows(
                InvalidDataException.class, () -> ticketAssigner.assignTicket("PAT-001", null));
        InvalidDataException level1 = assertThrows(
                InvalidDataException.class, () -> ticketAssigner.assignTicket("PAT-001", TriageLevel.LEVEL_1));

        assertEquals("DATOS_INVALIDOS", blankPatient.getErrorCode());
        assertEquals("DATOS_INVALIDOS", nullPatient.getErrorCode());
        assertEquals("DATOS_INVALIDOS", nullLevel.getErrorCode());
        assertEquals("DATOS_INVALIDOS", level1.getErrorCode());
        assertFalse(priorityQueueManager.patientExists("PAT-001"));
        assertTrue(priorityQueueManager.getOrderedList().isEmpty());
    }

    private static final class MutableClock extends Clock {

        private Instant currentInstant;
        private final ZoneId zone;

        private MutableClock(Instant initialInstant, ZoneId zone) {
            this.currentInstant = initialInstant;
            this.zone = zone;
        }

        private void advanceTo(Instant newInstant) {
            this.currentInstant = newInstant;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(currentInstant, zone);
        }

        @Override
        public Instant instant() {
            return currentInstant;
        }
    }
}