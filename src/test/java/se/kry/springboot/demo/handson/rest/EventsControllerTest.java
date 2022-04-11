package se.kry.springboot.demo.handson.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.services.EventService;

@WebFluxTest(EventsController.class)
class EventsControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EventService service;

  @Test
  void create_event() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    when(service.createEvent(any())).thenReturn(
        Mono.just(new Event(id, "Some event", start, end)));

    var payload = objectMapper.createObjectNode()
        .put("title", "Some Event")
        .put("start", "2001-01-01T00:00:00")
        .put("end", "2001-01-01T00:00:00")
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
  }

  @Test
  void create_event_with_blank_title() {
    var payload = objectMapper.createObjectNode()
        .put("title", " ")
        .put("start", "2001-01-01T00:00:00")
        .put("end", "2001-01-01T00:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void create_event_with_too_long_title() {
    var payload = objectMapper.createObjectNode()
        .put("title", "X".repeat(300))
        .put("start", "2001-01-01T00:00:00")
        .put("end", "2001-01-01T12:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void create_event_with_null_start() {
    var payload = objectMapper.createObjectNode()
        .put("title", "Some event")
        .put("end", "2001-01-01T00:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void create_event_with_null_end() {
    var payload = objectMapper.createObjectNode()
        .put("title", "Some event")
        .put("start", "2001-01-01T00:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void create_event_with_start_after_end() {
    var payload = objectMapper.createObjectNode()
        .put("title", "Some event")
        .put("start", "2001-01-01T12:00:00")
        .put("end", "2001-01-01T00:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void read_events() {
    webTestClient.get().uri("/api/v1/events")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void read_event() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    when(service.getEvent(id)).thenReturn(Mono.just(new Event(id, "Some event", start, end)));

    webTestClient.get().uri("/api/v1/events/{id}", id)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo("Some event")
        .jsonPath("$.start").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.end").isEqualTo("2001-01-01T12:00:00");
  }

  @Test
  void read_event_with_unknown_id() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    when(service.getEvent(id)).thenReturn(Mono.empty());

    webTestClient.get().uri("/api/v1/events/{id}", id)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_event() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", uuid)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void update_event_with_incorrect_id() {
    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", "foobar")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_event_with_unknown_id() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .toString();

    when(service.getEvent(uuid))
        .thenReturn(Mono.empty());

    webTestClient.patch().uri("/api/v1/events/{id}", uuid)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_event_with_too_long_title() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    var payload = objectMapper.createObjectNode()
        .put("title", "X".repeat(300))
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", uuid)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_event_with_start_after_end() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    var payload = objectMapper.createObjectNode()
        .put("start", "2001-01-01T12:00:00")
        .put("end", "2001-01-01T00:00:00")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", uuid)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void delete_event() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    webTestClient.delete().uri("/api/v1/events/{id}", uuid)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void delete_event_with_incorrect_id() {
    webTestClient.delete().uri("/api/v1/events/{id}", "foobar")
        .exchange()
        .expectStatus().isBadRequest();
  }
}