package com.adres.urgencias.turn.application;

import com.adres.urgencias.turn.domain.Patient;

/** Storage of the patients known by the ticket module (table {@code patient}). */
public interface PatientRepository {

    /** Inserts the patient, or updates its data and triage level if it already exists. */
    void save(Patient patient);
}
