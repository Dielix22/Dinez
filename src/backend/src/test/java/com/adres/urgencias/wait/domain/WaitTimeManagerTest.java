package com.adres.urgencias.wait.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.testsupport.InMemoryWaitLimitRepository;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// REQ-10: wait limits per level, level 1 excluded.
class WaitTimeManagerTest {

    private WaitTimeManager manager;

    @BeforeEach
    void setUp() {
        manager = new WaitTimeManager(new InMemoryWaitLimitRepository());
    }

    @Test
    void readsTheLimitFromStorage() {
        assertEquals(Duration.ofMinutes(60), manager.getLimit(TriageLevel.LEVEL_3));
    }

    @Test
    void changedLimitIsStoredAndReadBack() {
        manager.changeLimit(TriageLevel.LEVEL_3, Duration.ofMinutes(45));

        assertEquals(Duration.ofMinutes(45), manager.getLimit(TriageLevel.LEVEL_3));
    }

    @Test
    void rejectsLevelOneNullAndNonPositiveLimits() {
        assertThrows(InvalidDataException.class, () -> manager.getLimit(TriageLevel.LEVEL_1));
        assertThrows(InvalidDataException.class, () -> manager.getLimit(null));
        assertThrows(
                InvalidDataException.class,
                () -> manager.changeLimit(TriageLevel.LEVEL_2, Duration.ZERO));
        assertThrows(
                InvalidDataException.class, () -> manager.changeLimit(TriageLevel.LEVEL_2, null));
    }
}
