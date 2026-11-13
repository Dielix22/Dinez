package com.adres.urgencias.testsupport;

import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.domain.TicketCounterRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Fake daily counters for unit tests: no database. */
public class InMemoryTicketCounterRepository implements TicketCounterRepository {

    private final Map<List<Object>, Integer> counters = new HashMap<>();

    @Override
    public int nextNumber(LocalDate date, TriageLevel level) {
        return counters.merge(List.of(date, level), 1, Integer::sum);
    }
}
