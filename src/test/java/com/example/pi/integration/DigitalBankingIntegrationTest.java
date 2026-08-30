package com.example.pi.integration;

import com.example.pi.dto.request.LoginRequest;
import com.example.pi.dto.response.LoginResponse;
import com.example.pi.entity.BankAccount;
import com.example.pi.entity.User;
import com.example.pi.repository.BankAccountRepository;
import com.example.pi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test using a real MySQL container (doc item #58).
 *
 * This test class is only enabled when the system property
 * {@code it.docker.enabled=true} is set (e.g. in CI or when Docker Desktop is running):
 *
 * <pre>
 *   mvn test -Dit.docker.enabled=true
 * </pre>
 *
 * Flow under test:
 *  1. Seed an admin user + bank account directly via repositories
 *  2. POST /api/v1/auth/login → get JWT
 *  3. GET  /api/v1/customers/{id}/balance → assert 200 with authentication
 *  4. GET  unauthenticated request → assert 401/403
 *  5. POST /api/v1/auth/login with bad password → 401
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfSystemProperty(named = "it.docker.enabled", matches = "true",
        disabledReason = "Set -Dit.docker.enabled=true to run Docker-based integration tests")
class DigitalBankingIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("digitalbanking_it")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",        mysql::getJdbcUrl);
        registry.add("spring.datasource.username",   mysql::getUsername);
        registry.add("spring.datasource.password",   mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto",       () -> "create-drop");
        registry.add("spring.flyway.enabled",               () -> "false");
        registry.add("spring.jpa.properties.hibernate.dialect",
                     () -> "org.hibernate.dialect.MySQLDialect");
    }

    @LocalServerPort
    int port;

    @Autowired TestRestTemplate       restTemplate;
    @Autowired UserRepository         userRepository;
    @Autowired BankAccountRepository  bankAccountRepository;
    @Autowired PasswordEncoder        passwordEncoder;

    private static String jwtToken;
    private static final String USERNAME    = "it_admin";
    private static final String PASSWORD    = "Admin@12345";
    private static final int    CUSTOMER_ID = 1;

    @BeforeEach
    void seedData() {
        if (userRepository.findByUserName(USERNAME) == null) {
            User admin = new User(USERNAME, passwordEncoder.encode(PASSWORD), "ROLE_ADMIN");
            userRepository.save(admin);
        }
        if (bankAccountRepository.findByAccountNumber("IT-ACC-001").isEmpty()) {
            bankAccountRepository.save(
                    new BankAccount("IT-ACC-001", new BigDecimal("10000.00"), CUSTOMER_ID));
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void login_withValidCredentials_returnsJwt() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/login", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();

        jwtToken = response.getBody().getToken();
    }

    @Test
    @Order(2)
    void login_withWrongPassword_returns401() {
        LoginRequest bad = new LoginRequest(USERNAME, "wrongpassword1");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/login", bad, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(3)
    void getBalance_withValidToken_returnsOk() {
        Assumptions.assumeTrue(jwtToken != null, "JWT not yet obtained — skipping");

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/v1/customers/" + CUSTOMER_ID + "/balance",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Order(4)
    void getBalance_withoutToken_returns401Or403() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/api/v1/customers/" + CUSTOMER_ID + "/balance",
                String.class);

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    @Order(5)
    void loginEndpoint_isPermitAll() {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/login", loginRequest, String.class);

        // The endpoint is permitAll — it should not be blocked by Spring Security (403)
        assertThat(response.getStatusCode().value()).isNotEqualTo(403);
    }
}
