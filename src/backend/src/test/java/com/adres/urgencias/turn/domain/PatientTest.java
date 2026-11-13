package com.adres.urgencias.turn.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PatientTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 12, 8, 30);

    @Test
    void shouldCreatePatientAndUpdateTriageLevel() {
        Patient patient = new Patient("PAT-001", "10203040", "Ana", "Gomez", TriageLevel.LEVEL_3, NOW);

        assertEquals("PAT-001", patient.getPatientId());
        assertEquals("10203040", patient.getDocument());
        assertEquals("Ana", patient.getFirstName());
        assertEquals("Gomez", patient.getLastName());
        assertEquals(TriageLevel.LEVEL_3, patient.getTriageLevel());
        assertEquals(NOW, patient.getCreatedAt());

        patient.updateTriageLevel(TriageLevel.LEVEL_2);
        assertEquals(TriageLevel.LEVEL_2, patient.getTriageLevel());
    }

    @Test
    void shouldRejectBlankFieldsOrNullTriageLevel() {
        assertThrows(
                InvalidDataException.class,
                () -> new Patient(" ", "10203040", "Ana", "Gomez", TriageLevel.LEVEL_3, NOW));
        assertThrows(
                InvalidDataException.class,
                () -> new Patient("PAT-001", "", "Ana", "Gomez", TriageLevel.LEVEL_3, NOW));
        assertThrows(
                InvalidDataException.class,
                () -> new Patient("PAT-001", "10203040", null, "Gomez", TriageLevel.LEVEL_3, NOW));
        assertThrows(
                InvalidDataException.class,
                () -> new Patient("PAT-001", "10203040", "Ana", " ", TriageLevel.LEVEL_3, NOW));
        assertThrows(
                InvalidDataException.class, () -> new Patient("PAT-001", "10203040", "Ana", "Gomez", null, NOW));

        Patient valid = new Patient("PAT-001", "10203040", "Ana", "Gomez", TriageLevel.LEVEL_3, NOW);
        assertThrows(InvalidDataException.class, () -> valid.updateTriageLevel(null));
    }
}