package com.adres.urgencias.wait.domain;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.Duration;

/**
 * Manages configurable maximum wait limits per triage level (REQ-10, REQ-11).
 * Only levels 2 through 5 have a wait limit; level 1 requires immediate attention.
 * The limits are read from storage on demand, not kept here.
 */
public class WaitTimeManager {

    private final WaitLimitRepository limits;

    public WaitTimeManager(WaitLimitRepository limits) {
        if (limits == null) {
            throw new InvalidDataException("WaitLimitRepository must not be null");
        }
        this.limits = limits;
    }

    public Duration getLimit(TriageLevel level) {
        validateQueueLevel(level);
        return limits.findLimit(level);
    }

    public void changeLimit(TriageLevel level, Duration newLimit) {
        validateQueueLevel(level);
        if (newLimit == null || newLimit.isNegative() || newLimit.isZero()) {
            throw new InvalidDataException("Wait limit must be greater than zero");
        }
        limits.saveLimit(level, newLimit);
    }

    private void validateQueueLevel(TriageLevel level) {
        if (level == null || !level.hasQueue()) {
            throw new InvalidDataException("Wait limits only apply to triage levels 2 through 5");
        }
    }
}
