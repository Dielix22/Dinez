package com.adres.urgencias.queue.domain;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * FIFO queue for a single triage level between LEVEL_2 and LEVEL_5 (REQ-04, BR-TRG-02).
 */
public class LevelQueue {

    private final TriageLevel level;
    private final Deque<Ticket> tickets;

    public LevelQueue(TriageLevel level) {
        if (level == null || !level.hasQueue()) {
            throw new InvalidDataException("LevelQueue only supports levels 2 through 5");
        }
        this.level = level;
        this.tickets = new ArrayDeque<>();
    }

    public void enqueue(Ticket ticket) {
        if (ticket == null || ticket.getLevel() != this.level) {
            throw new InvalidDataException("Ticket does not match queue level " + level);
        }
        tickets.addLast(ticket);
    }

    public Optional<Ticket> peekFirst() {
        return Optional.ofNullable(tickets.peekFirst());
    }

    public boolean containsPatient(String patientId) {
        return tickets.stream().anyMatch(ticket -> ticket.getPatientId().equals(patientId));
    }

    public int nextPosition() {
        return tickets.stream()
                .mapToInt(Ticket::getPosition)
                .max()
                .orElse(0) + 1;
    }

    public List<Ticket> listAll() {
        return List.copyOf(new ArrayList<>(tickets));
    }

    public TriageLevel getLevel() {
        return level;
    }
}