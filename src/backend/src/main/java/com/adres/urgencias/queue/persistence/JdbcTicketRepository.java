package com.adres.urgencias.queue.persistence;

import com.adres.urgencias.queue.domain.TicketRepository;
import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** PostgreSQL implementation of {@link TicketRepository}. */
@Repository
public class JdbcTicketRepository implements TicketRepository {

    private static final String WAITING_PATIENT_INDEX = "uq_ticket_waiting_patient";

    private final JdbcClient jdbc;

    public JdbcTicketRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsWaitingForPatient(String patientId) {
        return jdbc.sql(
                                """
                                SELECT COUNT(*) FROM ticket
                                 WHERE patient_id = :patientId AND exit_time IS NULL
                                """)
                        .param("patientId", patientId)
                        .query(Integer.class)
                        .single()
                > 0;
    }

    @Override
    public int nextPosition(TriageLevel level) {
        return jdbc.sql(
                        """
                        SELECT COALESCE(MAX(position), 0) + 1 FROM ticket
                         WHERE level = :level AND exit_time IS NULL
                        """)
                .param("level", level.getLevel())
                .query(Integer.class)
                .single();
    }

    @Override
    public Ticket save(Ticket ticket) {
        try {
            long ticketId =
                    jdbc.sql(
                                    """
                                    INSERT INTO ticket
                                        (ticket_date, ticket_number, patient_id, arrival_time,
                                         level, position, exit_time)
                                    VALUES
                                        (:ticketDate, :ticketNumber, :patientId, :arrivalTime,
                                         :level, :position, :exitTime)
                                    RETURNING ticket_id
                                    """)
                            .param("ticketDate", ticket.getTicketDate())
                            .param("ticketNumber", ticket.getTicketNumber())
                            .param("patientId", ticket.getPatientId())
                            .param("arrivalTime", ticket.getArrivalTime())
                            .param("level", ticket.getLevel().getLevel())
                            .param("position", ticket.getPosition())
                            .param("exitTime", ticket.getExitTime().orElse(null))
                            .query(Long.class)
                            .single();
            return new Ticket(
                    ticketId,
                    ticket.getTicketDate(),
                    ticket.getTicketNumber(),
                    ticket.getPatientId(),
                    ticket.getArrivalTime(),
                    ticket.getLevel(),
                    ticket.getPosition(),
                    ticket.getExitTime().orElse(null));
        } catch (DuplicateKeyException e) {
            // Two concurrent requests for the same patient: the unique index decides.
            if (e.getMessage() != null && e.getMessage().contains(WAITING_PATIENT_INDEX)) {
                throw new DuplicatePatientException(ticket.getPatientId());
            }
            throw e;
        }
    }

    @Override
    public List<Ticket> findWaitingOrdered() {
        return jdbc.sql(
                        """
                        SELECT ticket_id, ticket_date, ticket_number, patient_id, arrival_time,
                               level, position, exit_time
                          FROM ticket
                         WHERE exit_time IS NULL
                         ORDER BY level, position
                        """)
                .query(JdbcTicketRepository::toTicket)
                .list();
    }

    private static Ticket toTicket(ResultSet row, int rowNumber) throws SQLException {
        Timestamp exitTime = row.getTimestamp("exit_time");
        LocalDateTime exit = exitTime == null ? null : exitTime.toLocalDateTime();
        return new Ticket(
                row.getLong("ticket_id"),
                row.getDate("ticket_date").toLocalDate(),
                row.getString("ticket_number"),
                row.getString("patient_id"),
                row.getTimestamp("arrival_time").toLocalDateTime(),
                TriageLevel.fromValue(row.getInt("level")),
                row.getInt("position"),
                exit);
    }
}
