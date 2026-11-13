package com.adres.urgencias.wait.domain;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/**
 * Manages configurable maximum wait limits per triage level (REQ-10, REQ-11).
 * Only levels 2 through 5 have a wait limit; level 1 requires immediate attention.
 */
public class WaitTimeManager {

    private final Map<TriageLevel, Duration> limits;

    public WaitTimeManager(Map<TriageLevel, Duration> initialLimits) {
        if (initialLimits == null) {
            throw new InvalidDataException("Initial wait limits map must not be null");
        }
        this.limits = new EnumMap<>(TriageLevel.class);
        for (TriageLevel level : TriageLevel.values()) {
            if (level.hasQueue()) {
                Duration limit = initialLimits.get(level);
                validateLimit(level, limit);
                this.limits.put(level, limit);
            }
        }
    }

    public Duration getLimit(TriageLevel level) {
        validateQueueLevel(level);
        return limits.get(level);
    }

    public void changeLimit(TriageLevel level, Duration newLimit) {
        validateLimit(level, newLimit);
        limits.put(level, newLimit);
    }

    private void validateQueueLevel(TriageLevel level) {
        if (level == null || !level.hasQueue()) {
            throw new InvalidDataException("Wait limits only apply to triage levels 2 through 5");
        }
    }

    private void validateLimit(TriageLevel level, Duration limit) {
        validateQueueLevel(level);
        if (limit == null || limit.isNegative() || limit.isZero()) {
            throw new InvalidDataException("Wait limit must be greater than zero");
        }
    }
}