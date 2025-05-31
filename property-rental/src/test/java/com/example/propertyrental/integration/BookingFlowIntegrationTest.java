package com.example.propertyrental.integration;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.propertyrental.kafka.PropertyEventProducer;
import com.example.propertyrental.service.ElasticPropertyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookingFlowIntegrationTest {

    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String REALM = "test-realm";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";
    private static final String POSTGRES_DOCKER_IMAGE_NAME = "postgres:15";
    private static final String DB_NAME = "test";
    private static final String DB_USERNAME = "test";
    private static final String DB_PASS = "test";

    // @MockBean
    // private PropertySearchRepository propertySearchRepository;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_DOCKER_IMAGE_NAME)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASS);
            
    @Container
    public static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:21.1.1")
    .withRealmImportFile("test-realm.json") .waitingFor(
        Wait.forHttp("/health/started")
            .forPort(8080)
            .withStartupTimeout(Duration.ofMinutes(2))
    );

    @Container
    public static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.10")
        .withEnv("discovery.type", "single-node")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    
    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1"))
    .withNetworkAliases("kafka")
    .withStartupTimeout(Duration.ofMinutes(2));

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    private String accessToken;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        if (!postgres.isRunning()) postgres.start();
        if (!keycloak.isRunning()) keycloak.start();

        if (!elasticsearch.isRunning()) elasticsearch.start();
        if (!kafka.isRunning()) kafka.start();
        
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/test-realm");
                registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeAll
    public void setup() {
        accessToken = fetchAccessToken(ADMIN_USER, ADMIN_PASS);
    }

    private String fetchAccessToken(String username, String password) {
        String tokenUrl = keycloak.getAuthServerUrl() + "/realms/"+REALM+"/protocol/openid-connect/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", CLIENT_ID);
        form.add("client_secret", CLIENT_SECRET);
        form.add("username", username);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, OAuthTokenResponse.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Failed to obtain access token");
        return response.getBody().getAccess_token();
    }

    private HttpHeaders getAuthHeaders(String username, String password) {
        String token = fetchAccessToken(username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<String> createProperty(HttpHeaders headers, PropertyDTO property) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(property);
        return restTemplate.postForEntity("/api/properties", new HttpEntity<>(json, headers), String.class);
    }

    private ResponseEntity<String> createBooking(HttpHeaders headers, BookingDTO booking) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(booking);
        return restTemplate.postForEntity("/api/bookings", new HttpEntity<>(json, headers), String.class);
    }

    @Test
    @DisplayName("Should create property and reject overlapping bookings with 409 Conflict")
    public void shouldCreatePropertyAndRejectOverlappingBooking() throws JsonProcessingException {
        HttpHeaders headers = getAuthHeaders(ADMIN_USER, ADMIN_PASS);

        PropertyDTO property = new PropertyDTO("Test Property", "Test City", 1000);
        ResponseEntity<String> propertyResponse = createProperty(headers, property);
        Assertions.assertEquals(HttpStatus.OK, propertyResponse.getStatusCode(), "Failed to create property");

        BookingDTO booking = new BookingDTO(1L, "2025-06-01T00:00:00", "2025-06-05T00:00:00");
        ResponseEntity<String> bookingResponse = createBooking(headers, booking);
        Assertions.assertEquals(HttpStatus.OK, bookingResponse.getStatusCode(), "Failed to create booking");

        BookingDTO conflictingBooking = new BookingDTO(1L, "2025-06-04T00:00:00", "2025-06-07T00:00:00");
        ResponseEntity<String> conflictResponse = createBooking(headers, conflictingBooking);
        Assertions.assertEquals(HttpStatus.CONFLICT, conflictResponse.getStatusCode(), "Overlapping booking was not rejected");
        Assertions.assertTrue(conflictResponse.getBody().contains("Overlapping booking exists"), "Conflict message missing");
    }

    @Test
    @DisplayName("Should access secured endpoint with valid Keycloak token")
    public void shouldAccessSecuredEndpointWithValidToken() {
        HttpHeaders headers = getAuthHeaders(ADMIN_USER, ADMIN_PASS);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/properties",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Should reject access to secured endpoint without token")
    public void shouldRejectAccessWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/properties", String.class);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // DTO classes for test payloads

    public static class PropertyDTO {
        private String title;
        private String location;
        private int price;

        public PropertyDTO() {}
        public PropertyDTO(String title, String location, int price) {
            this.title = title;
            this.location = location;
            this.price = price;
        }
        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public int getPrice() { return price; }
        public void setPrice(int price) { this.price = price; }
    }

    public static class BookingDTO {
        private Long propertyId;
        private String startDate;
        private String endDate;

        public BookingDTO() {}
        public BookingDTO(Long propertyId, String startDate, String endDate) {
            this.propertyId = propertyId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        // getters and setters
        public Long getPropertyId() { return propertyId; }
        public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }

    public static class OAuthTokenResponse {
        private String access_token;
        private String token_type;
        private int expires_in;
        private int refresh_expires_in;
        private String refresh_token;
        private String scope;

        public String getAccess_token() { return access_token; }
        public void setAccess_token(String access_token) { this.access_token = access_token; }
        // other getters/setters omitted for brevity
    }
}