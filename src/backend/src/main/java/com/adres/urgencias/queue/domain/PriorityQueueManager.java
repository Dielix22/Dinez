package com.adres.urgencias.queue.domain;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.Ticket;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Facade managing the per-level priority queues and queue positions (REQ-04, REQ-05, REQ-11, BR-TRG-02).
 */
public class PriorityQueueManager {

    private final Map<TriageLevel, LevelQueue> queues;
    private final WaitTimeManager waitTimeManager;

    public PriorityQueueManager(WaitTimeManager waitTimeManager) {
        if (waitTimeManager == null) {
            throw new InvalidDataException("WaitTimeManager must not be null");
        }
        this.waitTimeManager = waitTimeManager;
        this.queues = new EnumMap<>(TriageLevel.class);
        for (TriageLevel level : TriageLevel.values()) {
            if (level.hasQueue()) {
                this.queues.put(level, new LevelQueue(level));
            }
        }
    }

    /**
     * Verifies the patient is not already waiting and enqueues the ticket at the end of its level (REQ-04, BR-TRG-02).
     */
    public void addPatient(Ticket ticket) {
        if (ticket == null) {
            throw new InvalidDataException("Ticket must not be null");
        }
        if (patientExists(ticket.getPatientId())) {
            throw new DuplicatePatientException(ticket.getPatientId());
        }

        LevelQueue targetQueue = queues.get(ticket.getLevel());
        int assignedPosition = targetQueue.nextPosition();
        ticket.assignPosition(assignedPosition);
        targetQueue.enqueue(ticket);
    }

    public boolean patientExists(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            return false;
        }
        return queues.values().stream().anyMatch(queue -> queue.containsPatient(patientId));
    }

    public List<Ticket> getOrderedList() {
        List<Ticket> ordered = new ArrayList<>();
        for (TriageLevel level : List.of(
                TriageLevel.LEVEL_2, TriageLevel.LEVEL_3, TriageLevel.LEVEL_4, TriageLevel.LEVEL_5)) {
            ordered.addAll(queues.get(level).listAll());
        }
        return List.copyOf(ordered);
    }

    public WaitTimeManager getWaitTimeManager() {
        return waitTimeManager;
    }
}