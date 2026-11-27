package com.adres.urgencias.config;

import com.adres.urgencias.service.queue.PriorityQueueManager;
import com.adres.urgencias.queue.domain.TicketRepository;
import com.adres.urgencias.service.turn.TicketAssigner;
import com.adres.urgencias.turn.domain.TicketCounterRepository;
import com.adres.urgencias.wait.domain.WaitLimitRepository;
import com.adres.urgencias.service.wait.WaitTimeManager;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Creates the domain objects of the ticket module. They keep no state: it is in the database. */
@Configuration
public class TurnConfig {

    /**
     * Clock of the hospital. Its time zone decides when a day ends, that is, when the ticket
     * numbers restart (BR-TRG-04).
     */
    @Bean
    Clock clock(@Value("${app.time-zone:America/Bogota}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }

    @Bean
    WaitTimeManager waitTimeManager(WaitLimitRepository waitLimits) {
        return new WaitTimeManager(waitLimits);
    }

    @Bean
    PriorityQueueManager priorityQueueManager(
            TicketRepository tickets, WaitTimeManager waitTimeManager) {
        return new PriorityQueueManager(tickets, waitTimeManager);
    }

    @Bean
    TicketAssigner ticketAssigner(
            PriorityQueueManager priorityQueueManager,
            TicketCounterRepository counters,
            Clock clock) {
        return new TicketAssigner(priorityQueueManager, counters, clock);
    }
}
