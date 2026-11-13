package com.adres.urgencias.turn.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.domain.Patient;
import com.adres.urgencias.turn.domain.TicketAssigner;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * What the API does around the ticket assigner (REQ-04): store the patient, then assign. Uses the
 * real domain objects and a fake patient storage; no Spring, no database.
 */
class TicketAssignmentServiceTest {

    private static final ZoneId HOSPITAL_ZONE = ZoneId.of("America/Bogota");
    /** 10:00 in Bogotá on 12 November 2026. */
    private static final Instant NOW = Instant.parse("2026-11-12T15:00:00Z");

    private InMemoryPatientRepository patients;
    private TicketAssignmentService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, HOSPITAL_ZONE);
        WaitTimeManager waitTimeManager =
                new WaitTimeManager(
                        Map.of(
                                TriageLevel.LEVEL_2, Duration.ofMinutes(30),
                                TriageLevel.LEVEL_3, Duration.ofMinutes(60),
                                TriageLevel.LEVEL_4, Duration.ofMinutes(120),
                                TriageLevel.LEVEL_5, Duration.ofMinutes(240)));
        PriorityQueueManager priorityQueueManager = new PriorityQueueManager(waitTimeManager);
        TicketAssigner ticketAssigner = new TicketAssigner(priorityQueueManager, clock);
        patients = new InMemoryPatientRepository();
        service =
                new TicketAssignmentService(patients, ticketAssigner, priorityQueueManager, clock);
    }

    private TicketAssignmentResult assign(String patientId, Integer level) {
        return service.assignTicket(patientId, "79456123", "Jorge", "Castaño", level);
    }

    @Test
    void storesThePatientAndAssignsTheTicket() {
        TicketAssignmentResult result = assign("P-1001", 3);

        assertFalse(result.isImmediateCare());
        assertEquals("P-1001", result.patientId());
        assertEquals(3, result.level());
        assertEquals("T3-001", result.ticket().ticketNumber());
        assertEquals(1, result.ticket().position());
        Patient stored = patients.find("P-1001");
        assertEquals("79456123", stored.getDocument());
        assertEquals("Jorge", stored.getFirstName());
        assertEquals("Castaño", stored.getLastName());
        assertEquals(TriageLevel.LEVEL_3, stored.getTriageLevel());
    }

    @Test
    void arrivalTimeIsAnInstantWhateverTheHospitalTimeZone() {
        TicketAssignmentResult result = assign("P-1001", 3);

        assertEquals(NOW, result.ticket().arrivalTime());
    }

    // REQ-04 scenario 2 / BR-TRG-03.
    @Test
    void levelOneStoresThePatientButAssignsNoTicket() {
        TicketAssignmentResult result = assign("P-1001", 1);

        assertTrue(result.isImmediateCare());
        assertNull(result.ticket());
        assertEquals(1, result.level());
        assertEquals(TriageLevel.LEVEL_1, patients.find("P-1001").getTriageLevel());
        assertTrue(service.listWaitingTickets().isEmpty());
    }

    // REQ-04 scenario 4.
    @Test
    void waitingPatientIsRejectedAndItsStoredDataIsNotTouched() {
        assign("P-1001", 3);

        assertThrows(
                DuplicatePatientException.class,
                () -> service.assignTicket("P-1001", "000", "Other", "Name", 5));
        assertThrows(DuplicatePatientException.class, () -> assign("P-1001", 1));

        assertEquals(1, service.listWaitingTickets().size());
        assertEquals("Jorge", patients.find("P-1001").getFirstName());
        assertEquals(TriageLevel.LEVEL_3, patients.find("P-1001").getTriageLevel());
    }

    // REQ-04 scenario 5.
    @Test
    void invalidDataIsRejectedBeforeAnythingIsStored() {
        assertThrows(InvalidDataException.class, () -> assign("P-1001", null));
        assertThrows(InvalidDataException.class, () -> assign("P-1001", 0));
        assertThrows(InvalidDataException.class, () -> assign("P-1001", 6));
        assertThrows(InvalidDataException.class, () -> assign("   ", 3));
        assertThrows(InvalidDataException.class, () -> assign(null, 3));
        assertThrows(
                InvalidDataException.class,
                () -> service.assignTicket("P-1001", " ", "Jorge", "Castaño", 3));
        assertThrows(
                InvalidDataException.class,
                () -> service.assignTicket("P-1001", "79456123", null, "Castaño", 3));
        assertThrows(
                InvalidDataException.class,
                () -> service.assignTicket("P-1001", "79456123", "Jorge", "", 3));

        assertTrue(patients.isEmpty());
        assertTrue(service.listWaitingTickets().isEmpty());
    }

    @Test
    void surroundingSpacesAreRemovedSoTheyDoNotHideADuplicate() {
        assign("P-1001", 3);

        assertThrows(DuplicatePatientException.class, () -> assign("  P-1001 ", 3));
    }

    // BR-TRG-02.
    @Test
    void listsMostUrgentLevelFirstAndArrivalOrderWithinALevel() {
        assign("P-1", 4);
        assign("P-2", 3);
        assign("P-3", 3);
        assign("P-4", 2);

        List<String> numbers =
                service.listWaitingTickets().stream().map(WaitingTicket::ticketNumber).toList();

        assertEquals(List.of("T2-001", "T3-001", "T3-002", "T4-001"), numbers);
    }

    /** Fake storage: no database. */
    private static final class InMemoryPatientRepository implements PatientRepository {

        private final Map<String, Patient> patients = new HashMap<>();

        @Override
        public void save(Patient patient) {
            patients.put(patient.getPatientId(), patient);
        }

        Patient find(String patientId) {
            return patients.get(patientId);
        }

        boolean isEmpty() {
            return patients.isEmpty();
        }
    }
}
