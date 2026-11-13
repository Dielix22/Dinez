package com.adres.urgencias.shared.domain;

/**
 * Thrown when input data or domain invariants are violated (REQ-04, REQ-10).
 */
public class InvalidDataException extends RuntimeException {

    private static final String ERROR_CODE = "DATOS_INVALIDOS";

    public InvalidDataException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return ERROR_CODE;
    }
}