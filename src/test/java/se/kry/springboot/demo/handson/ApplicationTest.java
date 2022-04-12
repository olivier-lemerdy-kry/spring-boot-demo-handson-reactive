package se.kry.springboot.demo.handson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTest {

  @Container
  private static final MySQLContainer<?> mySql = new MySQLContainer<>("mysql:8");

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private EventRepository repository;

  @DynamicPropertySource
  static void mySqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mySql::getJdbcUrl);
    registry.add("spring.datasource.username", mySql::getUsername);
    registry.add("spring.datasource.password", mySql::getPassword);
  }

  @Test
  @Order(1)
  void create_event() throws Exception {
    assertRepositoryCountIs(0);

    var payload = objectMapper.createObjectNode()
        .put("title", "Some event")
        .put("start", "2001-01-01T00:00:00")
        .put("end", "2001-01-01T12:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some event")
        .jsonPath("$.start").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.end").isEqualTo("2001-01-01T12:00:00");

    assertRepositoryCountIs(1);
  }

  @Test
  @Order(2)
  void read_events() {
    assertRepositoryCountIs(1);

    webTestClient.get().uri("/api/v1/events")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.content").isArray()
        .jsonPath("$.content[0].title").isEqualTo("Some event")
        .jsonPath("$.content[0].start").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.content[0].end").isEqualTo("2001-01-01T12:00:00");
  }

  @Test
  @Order(3)
  void update_event() throws Exception {
    assertRepositoryCountIs(1);

    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .put("start", "2001-01-01T01:00:00")
        .put("end", "2001-01-01T13:00:00")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", findFirstEventId())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some other event")
        .jsonPath("$.start").isEqualTo("2001-01-01T01:00:00")
        .jsonPath("$.end").isEqualTo("2001-01-01T13:00:00");
  }

  @Test
  @Order(4)
  void read_event() {
    assertRepositoryCountIs(1);

    webTestClient.get().uri("/api/v1/events/{id}", findFirstEventId())
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some other event")
        .jsonPath("$.start").isEqualTo("2001-01-01T01:00:00")
        .jsonPath("$.end").isEqualTo("2001-01-01T13:00:00");
  }

  @Test
  @Order(5)
  void delete_event() {
    assertRepositoryCountIs(1);

    webTestClient.delete().uri("/api/v1/events/{id}", findFirstEventId())
        .exchange()
        .expectStatus().isOk();

    assertRepositoryCountIs(0);
  }

  private void assertRepositoryCountIs(long value) {
    repository.count()
        .as(StepVerifier::create)
        .assertNext(count -> assertThat(count).isEqualTo(value))
        .verifyComplete();
  }

  private UUID findFirstEventId() {
    return Optional.ofNullable(
            repository.findBy(Pageable.ofSize(1)).blockFirst())
        .map(Event::getId)
        .orElseThrow();
  }
}
