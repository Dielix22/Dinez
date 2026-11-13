package com.adres.urgencias.queue.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriorityQueueManagerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 12);
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 12, 9, 0);

    private PriorityQueueManager queueManager;

    @BeforeEach
    void setUp() {
        WaitTimeManager waitTimeManager = new WaitTimeManager(Map.of(
                TriageLevel.LEVEL_2, Duration.ofMinutes(30),
                TriageLevel.LEVEL_3, Duration.ofMinutes(60),
                TriageLevel.LEVEL_4, Duration.ofMinutes(120),
                TriageLevel.LEVEL_5, Duration.ofMinutes(240)));
        queueManager = new PriorityQueueManager(waitTimeManager);
    }

    @Test
    void shouldAssignSequentialPositionsPerLevelAndOrderByUrgencyAndArrival() {
        Ticket level4First = new Ticket(null, DATE, "T4-001", "PAT-4A", BASE_TIME, TriageLevel.LEVEL_4, 1, null);
        Ticket level2First =
                new Ticket(null, DATE, "T2-001", "PAT-2A", BASE_TIME.plusMinutes(1), TriageLevel.LEVEL_2, 1, null);
        Ticket level2Second =
                new Ticket(null, DATE, "T2-002", "PAT-2B", BASE_TIME.plusMinutes(2), TriageLevel.LEVEL_2, 1, null);

        queueManager.addPatient(level4First);
        queueManager.addPatient(level2First);
        queueManager.addPatient(level2Second);

        assertEquals(1, level4First.getPosition());
        assertEquals(1, level2First.getPosition());
        assertEquals(2, level2Second.getPosition());
        assertTrue(queueManager.patientExists("PAT-2B"));
        assertFalse(queueManager.patientExists("PAT-999"));

        List<Ticket> ordered = queueManager.getOrderedList();
        assertEquals(List.of(level2First, level2Second, level4First), ordered);
    }

    @Test
    void shouldRejectDuplicatePatientInSameOrDifferentLevel() {
        Ticket first = new Ticket(null, DATE, "T3-001", "PAT-100", BASE_TIME, TriageLevel.LEVEL_3, 1, null);
        Ticket duplicateDifferentLevel =
                new Ticket(null, DATE, "T2-001", "PAT-100", BASE_TIME.plusMinutes(5), TriageLevel.LEVEL_2, 1, null);

        queueManager.addPatient(first);

        DuplicatePatientException exception =
                assertThrows(DuplicatePatientException.class, () -> queueManager.addPatient(duplicateDifferentLevel));
        assertEquals("PACIENTE_DUPLICADO", exception.getErrorCode());
    }

    @Test
    void shouldRejectNullTicketOrInvalidQueueLevel() {
        assertThrows(InvalidDataException.class, () -> queueManager.addPatient(null));
        assertThrows(InvalidDataException.class, () -> new LevelQueue(TriageLevel.LEVEL_1));
    }
}