package com.adres.urgencias.queue.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.testsupport.InMemoryTicketRepository;
import com.adres.urgencias.testsupport.InMemoryWaitLimitRepository;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriorityQueueManagerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 12);
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 11, 12, 9, 0);

    private PriorityQueueManager queueManager;

    @BeforeEach
    void setUp() {
        queueManager =
                new PriorityQueueManager(
                        new InMemoryTicketRepository(),
                        new WaitTimeManager(new InMemoryWaitLimitRepository()));
    }

    private static Ticket ticket(String number, String patientId, int minute, TriageLevel level) {
        return new Ticket(null, DATE, number, patientId, BASE_TIME.plusMinutes(minute), level, 1, null);
    }

    // REQ-04, BR-TRG-02
    @Test
    void shouldAssignSequentialPositionsPerLevelAndOrderByUrgencyAndArrival() {
        Ticket level4First = queueManager.addPatient(ticket("T4-001", "PAT-4A", 0, TriageLevel.LEVEL_4));
        Ticket level2First = queueManager.addPatient(ticket("T2-001", "PAT-2A", 1, TriageLevel.LEVEL_2));
        Ticket level2Second = queueManager.addPatient(ticket("T2-002", "PAT-2B", 2, TriageLevel.LEVEL_2));

        assertEquals(1, level4First.getPosition());
        assertEquals(1, level2First.getPosition());
        assertEquals(2, level2Second.getPosition());
        assertTrue(queueManager.patientExists("PAT-2B"));
        assertFalse(queueManager.patientExists("PAT-999"));

        List<Ticket> ordered = queueManager.getOrderedList();
        assertEquals(List.of(level2First, level2Second, level4First), ordered);
    }

    // REQ-04 scenario 4
    @Test
    void shouldRejectDuplicatePatientInSameOrDifferentLevel() {
        queueManager.addPatient(ticket("T3-001", "PAT-100", 0, TriageLevel.LEVEL_3));
        Ticket duplicateDifferentLevel = ticket("T2-001", "PAT-100", 5, TriageLevel.LEVEL_2);

        DuplicatePatientException exception =
                assertThrows(DuplicatePatientException.class, () -> queueManager.addPatient(duplicateDifferentLevel));
        assertEquals("PACIENTE_DUPLICADO", exception.getErrorCode());
    }

    @Test
    void shouldRejectNullTicketAndLevelOneTicket() {
        assertThrows(InvalidDataException.class, () -> queueManager.addPatient(null));
        assertThrows(InvalidDataException.class, () -> ticket("T1-001", "PAT-1", 0, TriageLevel.LEVEL_1));
    }
}
