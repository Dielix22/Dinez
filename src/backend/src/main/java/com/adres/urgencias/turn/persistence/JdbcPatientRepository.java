package com.adres.urgencias.turn.persistence;

import com.adres.urgencias.turn.application.PatientRepository;
import com.adres.urgencias.turn.domain.Patient;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** PostgreSQL implementation of {@link PatientRepository}. */
@Repository
public class JdbcPatientRepository implements PatientRepository {

    private final JdbcClient jdbc;

    public JdbcPatientRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Patient patient) {
        // created_at is kept from the first time the module saw the patient.
        jdbc.sql(
                        """
                        INSERT INTO patient
                            (patient_id, document, first_name, last_name, triage_level, created_at)
                        VALUES
                            (:patientId, :document, :firstName, :lastName, :triageLevel, :createdAt)
                        ON CONFLICT (patient_id) DO UPDATE
                           SET document = EXCLUDED.document,
                               first_name = EXCLUDED.first_name,
                               last_name = EXCLUDED.last_name,
                               triage_level = EXCLUDED.triage_level
                        """)
                .param("patientId", patient.getPatientId())
                .param("document", patient.getDocument())
                .param("firstName", patient.getFirstName())
                .param("lastName", patient.getLastName())
                .param("triageLevel", patient.getTriageLevel().getLevel())
                .param("createdAt", patient.getCreatedAt())
                .update();
    }
}
