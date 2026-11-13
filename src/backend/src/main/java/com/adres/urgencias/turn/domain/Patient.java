package com.adres.urgencias.turn.domain;

import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.shared.domain.TriageLevel;
import java.time.LocalDateTime;

/**
 * Patient identification and active triage level within the Ticket Management module (REQ-04, REQ-05).
 */
public class Patient {

    /** Same limits as the columns of table {@code patient} (migration V1). */
    public static final int MAX_ID_LENGTH = 50;
    public static final int MAX_DOCUMENT_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 100;

    private final String patientId;
    private final String document;
    private final String firstName;
    private final String lastName;
    private TriageLevel triageLevel;
    private final LocalDateTime createdAt;

    public Patient(
            String patientId,
            String document,
            String firstName,
            String lastName,
            TriageLevel triageLevel,
            LocalDateTime createdAt) {
        if (patientId == null || patientId.isBlank()) {
            throw new InvalidDataException("Patient ID must not be blank");
        }
        if (document == null || document.isBlank()) {
            throw new InvalidDataException("Document must not be blank");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidDataException("First name must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidDataException("Last name must not be blank");
        }
        if (patientId.length() > MAX_ID_LENGTH
                || document.length() > MAX_DOCUMENT_LENGTH
                || firstName.length() > MAX_NAME_LENGTH
                || lastName.length() > MAX_NAME_LENGTH) {
            throw new InvalidDataException("A value is longer than the allowed length");
        }
        if (triageLevel == null) {
            throw new InvalidDataException("Triage level must not be null");
        }
        if (createdAt == null) {
            throw new InvalidDataException("Creation timestamp must not be null");
        }

        this.patientId = patientId;
        this.document = document;
        this.firstName = firstName;
        this.lastName = lastName;
        this.triageLevel = triageLevel;
        this.createdAt = createdAt;
    }

    public void updateTriageLevel(TriageLevel newLevel) {
        if (newLevel == null) {
            throw new InvalidDataException("New triage level must not be null");
        }
        this.triageLevel = newLevel;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDocument() {
        return document;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public TriageLevel getTriageLevel() {
        return triageLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}