package com.adres.urgencias.wait.persistence;

import com.adres.urgencias.shared.domain.TriageLevel;
import com.adres.urgencias.wait.domain.WaitLimitRepository;
import java.time.Duration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** PostgreSQL implementation of {@link WaitLimitRepository}. */
@Repository
public class JdbcWaitLimitRepository implements WaitLimitRepository {

    private final JdbcClient jdbc;

    public JdbcWaitLimitRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Duration findLimit(TriageLevel level) {
        int minutes =
                jdbc.sql("SELECT limit_minutes FROM wait_limit WHERE level = :level")
                        .param("level", level.getLevel())
                        .query(Integer.class)
                        .single();
        return Duration.ofMinutes(minutes);
    }

    @Override
    public void saveLimit(TriageLevel level, Duration limit) {
        jdbc.sql("UPDATE wait_limit SET limit_minutes = :minutes WHERE level = :level")
                .param("minutes", limit.toMinutes())
                .param("level", level.getLevel())
                .update();
    }
}
