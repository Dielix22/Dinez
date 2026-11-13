package com.adres.urgencias.wait.domain;

import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.Duration;

/** Storage of the maximum wait per level (table {@code wait_limit}, REQ-10). */
public interface WaitLimitRepository {

    /** Limit of a level from 2 to 5. */
    Duration findLimit(TriageLevel level);

    void saveLimit(TriageLevel level, Duration limit);
}
