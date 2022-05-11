package se.kry.springboot.demo.handson.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
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
        Single.just(new EventResponse(id, "Some event", start, end)));

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
        .expectHeader().valueEquals("Location", "/api/v1/events/38a14a82-d5a2-4210-9d61-cc3577bfa5df")
        .expectBody()
        .jsonPath("$.id").isEqualTo("38a14a82-d5a2-4210-9d61-cc3577bfa5df")
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
    var uuid1 = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start1 = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end1 = start1.plusHours(12);

    var uuid2 = UUID.fromString("8ebea9a7-e0ef-4a62-a729-aff26134f9d8");
    var start2 = start1.plusHours(1);
    var end2 = end1.plusHours(1);

    var content = List.of(
        new EventResponse(uuid1, "Some event", start1, end1),
        new EventResponse(uuid2, "Some other event", start2, end2)
    );

    var pageable = PageRequest.ofSize(20);

    when(service.getEvents(pageable))
        .thenReturn(Single.just(new PageImpl<>(content, pageable, content.size())));

    webTestClient.get().uri("/api/v1/events")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isMap()
        .jsonPath("$.content").isArray()
        .jsonPath("$.content[0].id").isEqualTo("38a14a82-d5a2-4210-9d61-cc3577bfa5df")
        .jsonPath("$.content[0].title").isEqualTo("Some event")
        .jsonPath("$.content[0].start").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.content[0].end").isEqualTo("2001-01-01T12:00:00")
        .jsonPath("$.content[1].id").isEqualTo("8ebea9a7-e0ef-4a62-a729-aff26134f9d8")
        .jsonPath("$.content[1].title").isEqualTo("Some other event")
        .jsonPath("$.content[1].start").isEqualTo("2001-01-01T01:00:00")
        .jsonPath("$.content[1].end").isEqualTo("2001-01-01T13:00:00")
        .jsonPath("$.pageable").isMap()
        .jsonPath("$.pageable.sort").isMap()
        .jsonPath("$.pageable.sort.empty").isEqualTo(true)
        .jsonPath("$.pageable.sort.unsorted").isEqualTo(true)
        .jsonPath("$.pageable.sort.sorted").isEqualTo(false)
        .jsonPath("$.pageable.offset").isEqualTo(0)
        .jsonPath("$.pageable.pageNumber").isEqualTo(0)
        .jsonPath("$.pageable.pageSize").isEqualTo(20)
        .jsonPath("$.pageable.paged").isEqualTo(true)
        .jsonPath("$.pageable.unpaged").isEqualTo(false)
        .jsonPath("$.totalPages").isEqualTo(1)
        .jsonPath("$.totalElements").isEqualTo(2)
        .jsonPath("$.last").isEqualTo(true)
        .jsonPath("$.size").isEqualTo(20)
        .jsonPath("$.number").isEqualTo(0)
        .jsonPath("$.sort").isMap()
        .jsonPath("$.sort.empty").isEqualTo(true)
        .jsonPath("$.sort.unsorted").isEqualTo(true)
        .jsonPath("$.sort.sorted").isEqualTo(false)
        .jsonPath("$.numberOfElements").isEqualTo(2)
        .jsonPath("$.first").isEqualTo(true)
        .jsonPath("$.empty").isEqualTo(false);
  }

  @Test
  void read_event() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    when(service.getEvent(id)).thenReturn(
        Maybe.just(new EventResponse(id, "Some event", start, end)));

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

    when(service.getEvent(id)).thenReturn(Maybe.empty());

    webTestClient.get().uri("/api/v1/events/{id}", id)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_event() {
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    when(service.updateEvent(uuid,
        new EventUpdateRequest(Optional.of("Some other event"), Optional.empty(), Optional.empty())))
        .thenReturn(Maybe.just(new EventResponse(uuid, "Some other event", start, end)));

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

    when(service.updateEvent(eq(uuid), any()))
        .thenReturn(Maybe.empty());

    var payload = objectMapper.createObjectNode()
        .put("title", "Some other event")
        .toString();

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