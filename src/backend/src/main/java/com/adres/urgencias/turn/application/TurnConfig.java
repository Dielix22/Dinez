package com.adres.urgencias.turn.application;

import com.adres.urgencias.queue.domain.PriorityQueueManager;
import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.turn.domain.TicketAssigner;
import com.adres.urgencias.wait.domain.WaitTimeManager;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the domain objects of the ticket module, one instance each for the whole application:
 * they hold the queues and the daily counters in memory.
 */
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

    /**
     * Provisional wait limits, the same values as migration V3. They are not used by ticket
     * assignment; the queue manager only needs them to be built.
     */
    @Bean
    WaitTimeManager waitTimeManager() {
        return new WaitTimeManager(
                Map.of(
                        TriageLevel.LEVEL_2, Duration.ofMinutes(30),
                        TriageLevel.LEVEL_3, Duration.ofMinutes(60),
                        TriageLevel.LEVEL_4, Duration.ofMinutes(120),
                        TriageLevel.LEVEL_5, Duration.ofMinutes(240)));
    }

    @Bean
    PriorityQueueManager priorityQueueManager(WaitTimeManager waitTimeManager) {
        return new PriorityQueueManager(waitTimeManager);
    }

    @Bean
    TicketAssigner ticketAssigner(PriorityQueueManager priorityQueueManager, Clock clock) {
        return new TicketAssigner(priorityQueueManager, clock);
    }
}
