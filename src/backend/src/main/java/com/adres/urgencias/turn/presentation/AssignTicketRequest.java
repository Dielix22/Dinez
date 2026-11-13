package com.adres.urgencias.turn.presentation;

/**
 * Body of {@code POST /api/tickets}: the identification data Registration would give and the
 * level Triage would give. Values arrive as typed; they are validated further in.
 */
public record AssignTicketRequest(
        String patientId, String document, String firstName, String lastName, Integer level) {}
