package com.adres.urgencias.presentation;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps domain exceptions to the single error format of the API. The error codes are the ones the
 * domain exceptions define ({@code DATOS_INVALIDOS}, {@code PACIENTE_DUPLICADO}).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Same code as {@link InvalidDataException}, for invalid requests detected outside the domain. */
    static final String INVALID_DATA = "DATOS_INVALIDOS";

    @ExceptionHandler(InvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidData(InvalidDataException e) {
        return new ErrorResponse(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(DuplicatePatientException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicatePatient(DuplicatePatientException e) {
        return new ErrorResponse(e.getErrorCode(), e.getMessage());
    }

    /** The body is missing or is not valid JSON (for example, a level that is not a number). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException e) {
        return new ErrorResponse(INVALID_DATA, "Missing or malformed request body");
    }

    /** The database rejected a value, for example a text longer than its column. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRejectedByDatabase(DataIntegrityViolationException e) {
        return new ErrorResponse(INVALID_DATA, "A value is too long or not accepted");
    }
}
