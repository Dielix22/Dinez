package com.adres.urgencias.turn.presentation;

import com.adres.urgencias.turn.application.TicketAssignmentResult;
import com.adres.urgencias.turn.application.TicketAssignmentService;
import com.adres.urgencias.turn.application.WaitingTicket;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point of the ticket module. No business logic here. */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketAssignmentService tickets;

    public TicketController(TicketAssignmentService tickets) {
        this.tickets = tickets;
    }

    /** Answers 201 when a ticket was created and 200 when the patient goes to immediate care. */
    @PostMapping
    public ResponseEntity<TicketAssignmentResponse> assignTicket(
            @RequestBody AssignTicketRequest request) {
        TicketAssignmentResult result =
                tickets.assignTicket(
                        request.patientId(),
                        request.document(),
                        request.firstName(),
                        request.lastName(),
                        request.level());
        HttpStatus status = result.isImmediateCare() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(TicketAssignmentResponse.from(result));
    }

    @GetMapping
    public List<WaitingTicket> listWaitingTickets() {
        return tickets.listWaitingTickets();
    }
}
