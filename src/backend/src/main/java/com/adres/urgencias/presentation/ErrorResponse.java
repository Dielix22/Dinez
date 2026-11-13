package com.adres.urgencias.presentation;

/**
 * Single error format of the API. {@code code} is stable and is what the front end maps to the
 * Spanish text shown to the user; {@code message} is a technical detail in English.
 */
public record ErrorResponse(String code, String message) {}
