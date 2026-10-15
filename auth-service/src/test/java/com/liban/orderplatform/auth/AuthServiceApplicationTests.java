package com.liban.orderplatform.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class AuthServiceApplicationTests {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgres = new  PostgreSQLContainer<>(
			"postgres:16-alpine")
			.withDatabaseName("auth-test")
			.withUsername("test")
			.withPassword("test");

	@Test
	void contextLoads() {
	}

}
