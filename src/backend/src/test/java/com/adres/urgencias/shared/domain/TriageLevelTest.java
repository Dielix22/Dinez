package com.adres.urgencias.shared.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TriageLevelTest {

    @Test
    void shouldIdentifyQueueEligibilityAccordingToBrTrg03() {
        assertFalse(TriageLevel.LEVEL_1.hasQueue());
        assertTrue(TriageLevel.LEVEL_2.hasQueue());
        assertTrue(TriageLevel.LEVEL_3.hasQueue());
        assertTrue(TriageLevel.LEVEL_4.hasQueue());
        assertTrue(TriageLevel.LEVEL_5.hasQueue());
    }

    @Test
    void shouldResolveValidLevelsFromNumericValue() {
        assertEquals(TriageLevel.LEVEL_1, TriageLevel.fromValue(1));
        assertEquals(TriageLevel.LEVEL_2, TriageLevel.fromValue(2));
        assertEquals(TriageLevel.LEVEL_3, TriageLevel.fromValue(3));
        assertEquals(TriageLevel.LEVEL_4, TriageLevel.fromValue(4));
        assertEquals(TriageLevel.LEVEL_5, TriageLevel.fromValue(5));
    }

    @Test
    void shouldRejectOutOfRangeNumericValuesWithDatosInvalidos() {
        InvalidDataException low = assertThrows(InvalidDataException.class, () -> TriageLevel.fromValue(0));
        InvalidDataException high = assertThrows(InvalidDataException.class, () -> TriageLevel.fromValue(6));

        assertEquals("DATOS_INVALIDOS", low.getErrorCode());
        assertEquals("DATOS_INVALIDOS", high.getErrorCode());
    }
}