package se.kry.springboot.demo.handson;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationTest {

  @Value("classpath:se/kry/springboot/demo/handson/domain/EventCreationRequest.json")
  private Resource eventCreationRequestJson;

  @Value("classpath:se/kry/springboot/demo/handson/domain/EventUpdateRequest.json")
  private Resource eventUpdateRequestJson;

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private EventRepository repository;

  @Test
  @Order(1)
  void create_event() throws Exception {
    assertRepositoryCountIs(0);

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(readJson(eventCreationRequestJson))
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

    webTestClient.patch().uri("/api/v1/events/{id}", findFirstEventId())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(readJson(eventUpdateRequestJson))
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

  private byte[] readJson(Resource resource) throws IOException {
    try (InputStream json = resource.getInputStream()) {
      return json.readAllBytes();
    }
  }

  private UUID findFirstEventId() {
    return Optional.ofNullable(
            repository.findBy(Pageable.ofSize(1)).blockFirst())
        .map(Event::getId)
        .orElseThrow();
  }
}
