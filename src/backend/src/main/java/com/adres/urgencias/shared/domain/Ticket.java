package com.adres.urgencias.shared.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Ticket assigned to a patient for a visit, tracking its queue level, position, and retirement (REQ-04, REQ-11, CON-TRG-02).
 */
public class Ticket {

    private final Long ticketId;
    private final LocalDate ticketDate;
    private final String ticketNumber;
    private final String patientId;
    private final LocalDateTime arrivalTime;
    private TriageLevel level;
    private int position;
    private LocalDateTime exitTime;

    public Ticket(
            Long ticketId,
            LocalDate ticketDate,
            String ticketNumber,
            String patientId,
            LocalDateTime arrivalTime,
            TriageLevel level,
            int position,
            LocalDateTime exitTime) {
        if (ticketDate == null) {
            throw new InvalidDataException("Ticket date must not be null");
        }
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new InvalidDataException("Ticket number must not be blank");
        }
        if (patientId == null || patientId.isBlank()) {
            throw new InvalidDataException("Patient ID must not be blank");
        }
        if (arrivalTime == null) {
            throw new InvalidDataException("Arrival time must not be null");
        }
        if (level == null || !level.hasQueue()) {
            throw new InvalidDataException("Ticket level must be between LEVEL_2 and LEVEL_5");
        }
        if (position < 1) {
            throw new InvalidDataException("Queue position must be at least 1");
        }
        if (exitTime != null && exitTime.isBefore(arrivalTime)) {
            throw new InvalidDataException("Exit time cannot be before arrival time");
        }

        this.ticketId = ticketId;
        this.ticketDate = ticketDate;
        this.ticketNumber = ticketNumber;
        this.patientId = patientId;
        this.arrivalTime = arrivalTime;
        this.level = level;
        this.position = position;
        this.exitTime = exitTime;
    }

    /**
     * Sets the real queue position when enqueued by PriorityQueueManager (REQ-04).
     */
    public void assignPosition(int position) {
        if (!isWaiting()) {
            throw new InvalidDataException("Cannot assign queue position to a retired ticket");
        }
        if (position < 1) {
            throw new InvalidDataException("Queue position must be at least 1");
        }
        this.position = position;
    }

    /**
     * Evaluates whether this waiting ticket has exceeded the maximum allowed wait limit (REQ-11).
     */
    public boolean hasExceededLimit(Duration limit, LocalDateTime now) {
        if (limit == null || limit.isNegative() || limit.isZero() || now == null) {
            throw new InvalidDataException("Wait limit and current time must be valid");
        }
        return Duration.between(arrivalTime, now).compareTo(limit) > 0;
    }

    /**
     * Relocates this waiting ticket to a new triage level and queue position (REQ-05).
     */
    public void changeLevel(TriageLevel newLevel, int newPosition) {
        if (!isWaiting()) {
            throw new InvalidDataException("Cannot change level of a retired ticket");
        }
        if (newLevel == null || !newLevel.hasQueue()) {
            throw new InvalidDataException("Target level must be between LEVEL_2 and LEVEL_5");
        }
        if (newPosition < 1) {
            throw new InvalidDataException("Queue position must be at least 1");
        }
        this.level = newLevel;
        this.position = newPosition;
    }

    /**
     * Marks this ticket as retired from the waiting queue when called for care (REQ-11).
     */
    public void retire(LocalDateTime retiredAt) {
        if (!isWaiting()) {
            throw new InvalidDataException("Ticket is already retired");
        }
        if (retiredAt == null || retiredAt.isBefore(arrivalTime)) {
            throw new InvalidDataException("Exit time cannot be null or before arrival time");
        }
        this.exitTime = retiredAt;
    }

    public boolean isWaiting() {
        return exitTime == null;
    }

    public Optional<Long> getTicketId() {
        return Optional.ofNullable(ticketId);
    }

    public LocalDate getTicketDate() {
        return ticketDate;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getPatientId() {
        return patientId;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public TriageLevel getLevel() {
        return level;
    }

    public int getPosition() {
        return position;
    }

    public Optional<LocalDateTime> getExitTime() {
        return Optional.ofNullable(exitTime);
    }
}