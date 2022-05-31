package se.kry.springboot.demo.handson;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.kry.springboot.demo.handson.data.EventRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
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
  void scenario() throws IOException {
    var id = step1_create_event();
    step2_read_events();
    step3_update_event(id);
    step4_read_event(id);
    step5_delete_event(id);
  }

  UUID step1_create_event() throws IOException {
    assertRepositoryCountIs(0);

    var payload = objectMapper.createObjectNode()
        .put("title", "Some event")
        .put("startTime", "2001-01-01T00:00:00")
        .put("endTime", "2001-01-01T12:00:00")
        .toString();

    var result = webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some event")
        .jsonPath("$.startTime").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.endTime").isEqualTo("2001-01-01T12:00:00")
        .returnResult();

    assertRepositoryCountIs(1);

    try (InputStream stream = new ByteArrayInputStream(requireNonNull(result.getResponseBodyContent()))) {
      return UUID.fromString(JsonPath.read(stream, "$.id"));
    }
  }

  void step2_read_events() {
    assertRepositoryCountIs(1);

    webTestClient.get().uri("/api/v1/events")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.content").isArray()
        .jsonPath("$.content[0].title").isEqualTo("Some event")
        .jsonPath("$.content[0].startTime").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.content[0].endTime").isEqualTo("2001-01-01T12:00:00");
  }

  void step3_update_event(UUID id) {
    assertRepositoryCountIs(1);

    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .put("startTime", "2001-01-01T01:00:00")
        .put("endTime", "2001-01-01T13:00:00")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some other event")
        .jsonPath("$.startTime").isEqualTo("2001-01-01T01:00:00")
        .jsonPath("$.endTime").isEqualTo("2001-01-01T13:00:00");
  }

  void step4_read_event(UUID id) {
    assertRepositoryCountIs(1);

    webTestClient.get().uri("/api/v1/events/{id}", id)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some other event")
        .jsonPath("$.startTime").isEqualTo("2001-01-01T01:00:00")
        .jsonPath("$.endTime").isEqualTo("2001-01-01T13:00:00");
  }

  void step5_delete_event(UUID id) {
    assertRepositoryCountIs(1);

    webTestClient.delete().uri("/api/v1/events/{id}", id)
        .exchange()
        .expectStatus().isOk();

    assertRepositoryCountIs(0);
  }

  private void assertRepositoryCountIs(long value) {
    repository.count()
        .test()
        .assertValue(value)
        .assertComplete();
  }
}
