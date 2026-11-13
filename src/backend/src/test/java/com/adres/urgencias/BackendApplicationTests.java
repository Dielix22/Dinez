package com.adres.urgencias;

import com.adres.urgencias.persistence.PostgresContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

/** The whole application starts on PostgreSQL with the real migrations (ADR-0007). */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(PostgresContainerConfiguration.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
