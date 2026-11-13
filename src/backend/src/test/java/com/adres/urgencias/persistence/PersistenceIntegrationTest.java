package com.adres.urgencias.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.application.TicketAssignmentResult;
import com.adres.urgencias.turn.application.TicketAssignmentService;
import com.adres.urgencias.turn.application.WaitingTicket;
import com.adres.urgencias.turn.domain.TicketCounterRepository;
import com.adres.urgencias.wait.domain.WaitLimitRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Persistence layer against a real PostgreSQL with the Flyway migrations applied (ADR-0007).
 * Skipped when Docker is not available. Covers REQ-04, CON-TRG-02, CON-TRG-03, BR-TRG-04 and
 * REQ-10.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(PostgresContainerConfiguration.class)
class PersistenceIntegrationTest {

    private static final int CONCURRENT_REQUESTS = 8;

    @Autowired private TicketAssignmentService service;
    @Autowired private TicketCounterRepository counters;
    @Autowired private WaitLimitRepository waitLimits;
    @Autowired private JdbcClient jdbc;
    @Autowired private TransactionTemplate transaction;

    @BeforeEach
    void cleanTables() {
        jdbc.sql("DELETE FROM ticket").update();
        jdbc.sql("DELETE FROM ticket_counter").update();
        jdbc.sql("DELETE FROM patient").update();
    }

    private TicketAssignmentResult assign(String patientId, int level) {
        return service.assignTicket(patientId, "79456123", "Jorge", "Castaño", level);
    }

    private int lastNumber(LocalDate date, int level) {
        return jdbc.sql(
                        "SELECT COALESCE(MAX(last_number), 0) FROM ticket_counter"
                                + " WHERE ticket_date = :date AND level = :level")
                .param("date", date)
                .param("level", level)
                .query(Integer.class)
                .single();
    }

    private int ticketCount() {
        return jdbc.sql("SELECT COUNT(*) FROM ticket").query(Integer.class).single();
    }

    // CON-TRG-03: the queue and the numbering are in the database, not in memory.
    @Test
    void queueAndCounterAreReadFromTheDatabaseInQueueOrder() {
        assign("P-1", 4);
        assign("P-2", 3);
        assign("P-3", 3);
        assign("P-4", 2);

        List<WaitingTicket> waiting = service.listWaitingTickets();

        assertEquals(
                List.of("T2-001", "T3-001", "T3-002", "T4-001"),
                waiting.stream().map(WaitingTicket::ticketNumber).toList());
        assertEquals(2, lastNumber(waiting.get(1).ticketDate(), 3));
    }

    // BR-TRG-04: numbering restarts each day, same day continues.
    @Test
    void counterRestartsEachDayAndContinuesWithinTheDay() {
        LocalDate day = LocalDate.of(2026, 11, 12);

        assertEquals(1, counters.nextNumber(day, TriageLevel.LEVEL_3));
        assertEquals(2, counters.nextNumber(day, TriageLevel.LEVEL_3));
        assertEquals(1, counters.nextNumber(day, TriageLevel.LEVEL_4));
        assertEquals(1, counters.nextNumber(day.plusDays(1), TriageLevel.LEVEL_3));
    }

    // Contract 02 v2 section 3, postcondition 4: a rolled-back assignment consumes no number.
    @Test
    void rolledBackTransactionDoesNotConsumeANumber() {
        LocalDate day = LocalDate.of(2026, 11, 12);

        assertThrows(
                IllegalStateException.class,
                () ->
                        transaction.executeWithoutResult(
                                status -> {
                                    counters.nextNumber(day, TriageLevel.LEVEL_3);
                                    throw new IllegalStateException("force rollback");
                                }));

        assertEquals(0, lastNumber(day, 3));
    }

    // REQ-04 scenario 4: duplicate rejected, counter and queue untouched.
    @Test
    void duplicatePatientLeavesCounterAndTicketsUntouched() {
        TicketAssignmentResult first = assign("P-1", 3);

        assertThrows(DuplicatePatientException.class, () -> assign("P-1", 3));

        assertEquals(1, ticketCount());
        assertEquals(1, lastNumber(service.listWaitingTickets().get(0).ticketDate(), 3));
        assertEquals("T3-001", first.ticket().ticketNumber());
    }

    // Two simultaneous requests for the same patient: one ticket, one PACIENTE_DUPLICADO.
    @Test
    void concurrentRequestsForTheSamePatientCreateOneTicket() throws Exception {
        List<Callable<Object>> requests = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            requests.add(() -> assign("P-SAME", 3));
        }

        List<Object> outcomes = runConcurrently(requests);

        assertEquals(1, outcomes.stream().filter(o -> o instanceof TicketAssignmentResult).count());
        assertEquals(
                1, outcomes.stream().filter(o -> o instanceof DuplicatePatientException).count());
        assertEquals(1, ticketCount());
    }

    // CON-TRG-02: concurrent requests of one level get distinct numbers and positions.
    @Test
    void concurrentRequestsOfOneLevelGetDistinctNumbersAndPositions() throws Exception {
        List<Callable<Object>> requests = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            String patientId = "P-" + i;
            requests.add(() -> assign(patientId, 3));
        }

        runConcurrently(requests);

        List<WaitingTicket> waiting = service.listWaitingTickets();
        assertEquals(CONCURRENT_REQUESTS, waiting.size());
        assertEquals(
                CONCURRENT_REQUESTS,
                waiting.stream().map(WaitingTicket::ticketNumber).distinct().count());
        assertEquals(
                List.of(1, 2, 3, 4, 5, 6, 7, 8),
                waiting.stream().map(WaitingTicket::position).toList());
    }

    // REQ-10: limits come from wait_limit (defaults of migration V3); the schema guards them.
    @Test
    void waitLimitsAreReadFromTheDatabaseAndTheSchemaRejectsNonPositiveValues() {
        assertEquals(Duration.ofMinutes(30), waitLimits.findLimit(TriageLevel.LEVEL_2));
        assertEquals(Duration.ofMinutes(240), waitLimits.findLimit(TriageLevel.LEVEL_5));

        waitLimits.saveLimit(TriageLevel.LEVEL_3, Duration.ofMinutes(45));
        assertEquals(Duration.ofMinutes(45), waitLimits.findLimit(TriageLevel.LEVEL_3));
        waitLimits.saveLimit(TriageLevel.LEVEL_3, Duration.ofMinutes(60));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> waitLimits.saveLimit(TriageLevel.LEVEL_3, Duration.ZERO));
    }

    /** Runs the calls at the same time; returns each result or the exception it threw. */
    private static List<Object> runConcurrently(List<Callable<Object>> calls) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(calls.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<Object> call : calls) {
                futures.add(
                        executor.submit(
                                () -> {
                                    start.await();
                                    return call.call();
                                }));
            }
            start.countDown();
            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                try {
                    outcomes.add(future.get());
                } catch (ExecutionException e) {
                    outcomes.add(e.getCause());
                }
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }
}
