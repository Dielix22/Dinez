package com.adres.urgencias.testsupport;

import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.wait.domain.WaitLimitRepository;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

/** Fake wait limits for unit tests, with the same defaults as migration V3. */
public class InMemoryWaitLimitRepository implements WaitLimitRepository {

    private final Map<TriageLevel, Duration> limits = new EnumMap<>(TriageLevel.class);

    public InMemoryWaitLimitRepository() {
        limits.put(TriageLevel.LEVEL_2, Duration.ofMinutes(30));
        limits.put(TriageLevel.LEVEL_3, Duration.ofMinutes(60));
        limits.put(TriageLevel.LEVEL_4, Duration.ofMinutes(120));
        limits.put(TriageLevel.LEVEL_5, Duration.ofMinutes(240));
    }

    @Override
    public Duration findLimit(TriageLevel level) {
        return limits.get(level);
    }

    @Override
    public void saveLimit(TriageLevel level, Duration limit) {
        limits.put(level, limit);
    }
}
