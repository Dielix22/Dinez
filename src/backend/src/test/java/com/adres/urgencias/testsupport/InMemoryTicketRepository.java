package com.adres.urgencias.testsupport;

import com.adres.urgencias.queue.domain.TicketRepository;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Fake ticket storage for unit tests: no database. */
public class InMemoryTicketRepository implements TicketRepository {

    private final List<Ticket> tickets = new ArrayList<>();
    private long nextId = 1;

    @Override
    public boolean existsWaitingForPatient(String patientId) {
        return tickets.stream()
                .anyMatch(t -> t.isWaiting() && t.getPatientId().equals(patientId));
    }

    @Override
    public int nextPosition(TriageLevel level) {
        return tickets.stream()
                        .filter(t -> t.isWaiting() && t.getLevel() == level)
                        .mapToInt(Ticket::getPosition)
                        .max()
                        .orElse(0)
                + 1;
    }

    @Override
    public Ticket save(Ticket ticket) {
        Ticket stored =
                new Ticket(
                        nextId++,
                        ticket.getTicketDate(),
                        ticket.getTicketNumber(),
                        ticket.getPatientId(),
                        ticket.getArrivalTime(),
                        ticket.getLevel(),
                        ticket.getPosition(),
                        ticket.getExitTime().orElse(null));
        tickets.add(stored);
        return stored;
    }

    @Override
    public List<Ticket> findWaitingOrdered() {
        return tickets.stream()
                .filter(Ticket::isWaiting)
                .sorted(
                        Comparator.comparing((Ticket t) -> t.getLevel().getLevel())
                                .thenComparingInt(Ticket::getPosition))
                .toList();
    }

    public boolean isEmpty() {
        return tickets.isEmpty();
    }
}
