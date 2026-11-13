package com.adres.urgencias.turn.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adres.urgencias.shared.domain.DuplicatePatientException;
import com.adres.urgencias.shared.domain.InvalidDataException;
import com.adres.urgencias.turn.application.TicketAssignmentResult;
import com.adres.urgencias.turn.application.TicketAssignmentService;
import com.adres.urgencias.turn.application.WaitingTicket;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP contract of the ticket API: status codes, JSON fields and error codes. */
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    private static final String REQUEST =
            """
            {"patientId": "P-1001", "document": "79456123", "firstName": "Jorge",
             "lastName": "Castaño", "level": 3}
            """;
    private static final WaitingTicket TICKET =
            new WaitingTicket(LocalDate.of(2026, 11, 12), "T3-015", "P-1001", 3, 3, Instant.parse("2026-11-12T15:00:00Z"));

    @Autowired private MockMvc mockMvc;

    @MockitoBean private TicketAssignmentService tickets;

    @Test
    void assignedTicketAnswersCreatedWithTheTicket() throws Exception {
        when(tickets.assignTicket("P-1001", "79456123", "Jorge", "Castaño", 3))
                .thenReturn(new TicketAssignmentResult("P-1001", 3, TICKET));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketNumber").value("T3-015"))
                .andExpect(jsonPath("$.patientId").value("P-1001"))
                .andExpect(jsonPath("$.level").value(3))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.immediateCare").value(false))
                .andExpect(jsonPath("$.arrivalTime").value("2026-11-12T15:00:00Z"));
    }

    @Test
    void levelOneAnswersOkWithoutTicket() throws Exception {
        when(tickets.assignTicket(any(), any(), any(), any(), any()))
                .thenReturn(new TicketAssignmentResult("P-1001", 1, null));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.immediateCare").value(true))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.ticketNumber").isEmpty());
    }

    @Test
    void invalidDataAnswersBadRequestWithTheDomainCode() throws Exception {
        when(tickets.assignTicket(any(), any(), any(), any(), any()))
                .thenThrow(new InvalidDataException("Triage level must be between 1 and 5: 9"));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATOS_INVALIDOS"));
    }

    @Test
    void malformedBodyAnswersBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"level\": \"three\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATOS_INVALIDOS"));
    }

    @Test
    void duplicatePatientAnswersConflict() throws Exception {
        when(tickets.assignTicket(any(), any(), any(), any(), any()))
                .thenThrow(new DuplicatePatientException("P-1001"));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PACIENTE_DUPLICADO"));
    }

    @Test
    void listsWaitingTickets() throws Exception {
        when(tickets.listWaitingTickets()).thenReturn(List.of(TICKET));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketDate").value("2026-11-12"))
                .andExpect(jsonPath("$[0].ticketNumber").value("T3-015"))
                .andExpect(jsonPath("$[0].level").value(3))
                .andExpect(jsonPath("$[0].position").value(3));
    }
}
