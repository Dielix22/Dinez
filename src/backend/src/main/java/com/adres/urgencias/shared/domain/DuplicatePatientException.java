package com.adres.urgencias.shared.domain;

/**
 * Thrown when attempting to assign a ticket to a patient who already has a waiting ticket (REQ-04).
 */
public class DuplicatePatientException extends RuntimeException {

    private static final String ERROR_CODE = "PACIENTE_DUPLICADO";

    public DuplicatePatientException(String patientId) {
        super("Patient already has an active waiting ticket: " + patientId);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}