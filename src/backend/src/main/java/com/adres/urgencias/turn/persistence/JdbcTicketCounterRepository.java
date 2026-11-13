package com.adres.urgencias.turn.persistence;

import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.domain.TicketCounterRepository;
import java.time.LocalDate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** PostgreSQL implementation of {@link TicketCounterRepository}. */
@Repository
public class JdbcTicketCounterRepository implements TicketCounterRepository {

    private final JdbcClient jdbc;

    public JdbcTicketCounterRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int nextNumber(LocalDate date, TriageLevel level) {
        // The upsert locks the (day, level) row until the transaction ends: concurrent requests
        // of the same level are serialised and never get the same number.
        return jdbc.sql(
                        """
                        INSERT INTO ticket_counter (ticket_date, level, last_number)
                        VALUES (:ticketDate, :level, 1)
                        ON CONFLICT (ticket_date, level) DO UPDATE
                           SET last_number = ticket_counter.last_number + 1
                        RETURNING last_number
                        """)
                .param("ticketDate", date)
                .param("level", level.getLevel())
                .query(Integer.class)
                .single();
    }
}
