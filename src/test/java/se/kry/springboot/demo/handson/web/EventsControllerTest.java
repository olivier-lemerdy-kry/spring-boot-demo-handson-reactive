package se.kry.springboot.demo.handson.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.EventParticipantsUpdateRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
import se.kry.springboot.demo.handson.domain.PersonDefaults;
import se.kry.springboot.demo.handson.domain.PersonResponse;
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
    var startTime = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var endTime = startTime.plusHours(12);

    when(service.createEvent(any())).thenReturn(
        Mono.just(new EventResponse(EventDefaults.ID, EventDefaults.TITLE, startTime, endTime)));

    var payload = objectMapper.createObjectNode()
        .put("title", EventDefaults.TITLE)
        .put("startTime", "2001-01-01T00:00:00")
        .put("endTime", "2001-01-01T12:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().valueEquals("Location", "/api/v1/events/" + EventDefaults.ID)
        .expectBody()
        .jsonPath("$.id").isEqualTo(EventDefaults.ID_STRING)
        .jsonPath("$.title").isEqualTo(EventDefaults.TITLE)
        .jsonPath("$.startTime").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.endTime").isEqualTo("2001-01-01T12:00:00");
  }

  @Test
  void create_event_with_blank_title() {
    var payload = objectMapper.createObjectNode()
        .put("title", " ")
        .put("startTime", "2001-01-01T00:00:00")
        .put("endTime", "2001-01-01T00:00:00")
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
        .put("startTime", "2001-01-01T00:00:00")
        .put("endTime", "2001-01-01T12:00:00")
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
        .put("title", EventDefaults.TITLE)
        .put("endTime", "2001-01-01T00:00:00")
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
        .put("title", EventDefaults.TITLE)
        .put("startTime", "2001-01-01T00:00:00")
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
        .put("title", EventDefaults.TITLE)
        .put("startTime", "2001-01-01T12:00:00")
        .put("endTime", "2001-01-01T00:00:00")
        .toString();

    webTestClient.post().uri("/api/v1/events")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void read_events() {
    var start1 = EventDefaults.START_TIME;
    var end1 = EventDefaults.END_TIME;

    var uuid2 = UUID.fromString("8ebea9a7-e0ef-4a62-a729-aff26134f9d8");
    var start2 = start1.plusHours(1);
    var end2 = end1.plusHours(1);

    var content = List.of(
        new EventResponse(EventDefaults.ID, EventDefaults.TITLE, start1, end1),
        new EventResponse(uuid2, EventDefaults.OTHER_TITLE, start2, end2)
    );

    var pageable = PageRequest.ofSize(20);

    when(service.getEvents(pageable))
        .thenReturn(Mono.just(new PageImpl<>(content, pageable, content.size())));

    webTestClient.get().uri("/api/v1/events")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isMap()
        .jsonPath("$.content").isArray()
        .jsonPath("$.content[0].id").isEqualTo(EventDefaults.ID_STRING)
        .jsonPath("$.content[0].title").isEqualTo(EventDefaults.TITLE)
        .jsonPath("$.content[0].startTime").isEqualTo("2001-01-01T12:00:00")
        .jsonPath("$.content[0].endTime").isEqualTo("2001-01-01T13:00:00")
        .jsonPath("$.content[1].id").isEqualTo("8ebea9a7-e0ef-4a62-a729-aff26134f9d8")
        .jsonPath("$.content[1].title").isEqualTo(EventDefaults.OTHER_TITLE)
        .jsonPath("$.content[1].startTime").isEqualTo("2001-01-01T13:00:00")
        .jsonPath("$.content[1].endTime").isEqualTo("2001-01-01T14:00:00")
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
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    when(service.getEvent(EventDefaults.ID)).thenReturn(
        Mono.just(new EventResponse(EventDefaults.ID, EventDefaults.TITLE, start, end)));

    webTestClient.get().uri("/api/v1/events/{id}", EventDefaults.ID)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.title").isEqualTo(EventDefaults.TITLE)
        .jsonPath("$.startTime").isEqualTo("2001-01-01T00:00:00")
        .jsonPath("$.endTime").isEqualTo("2001-01-01T12:00:00");
  }

  @Test
  void read_event_with_unknown_id() {
    when(service.getEvent(EventDefaults.ID)).thenReturn(Mono.empty());

    webTestClient.get().uri("/api/v1/events/{id}", EventDefaults.ID)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_event() {
    var eventResponse =
        new EventResponse(EventDefaults.ID, EventDefaults.OTHER_TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME);
    var eventUpdateRequest =
        new EventUpdateRequest(Optional.of(EventDefaults.OTHER_TITLE), Optional.empty(), Optional.empty());
    when(service.updateEvent(EventDefaults.ID, eventUpdateRequest))
        .thenReturn(Mono.just(eventResponse));

    var payload = objectMapper.createObjectNode()
        .put("title", EventDefaults.OTHER_TITLE)
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", EventDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void update_event_with_incorrect_id() {
    var payload = objectMapper.createObjectNode()
        .put("title", EventDefaults.OTHER_TITLE)
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", "foobar")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_event_with_unknown_id() {
    when(service.updateEvent(eq(EventDefaults.ID), any()))
        .thenReturn(Mono.empty());

    var payload = objectMapper.createObjectNode()
        .put("title", EventDefaults.OTHER_TITLE)
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", EventDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_event_with_too_long_title() {
    var payload = objectMapper.createObjectNode()
        .put("title", "X".repeat(300))
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", EventDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_event_with_start_after_end() {
    var payload = objectMapper.createObjectNode()
        .put("startTime", "2001-01-01T12:00:00")
        .put("endTime", "2001-01-01T00:00:00")
        .toString();

    webTestClient.patch().uri("/api/v1/events/{id}", EventDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_event_participants() {
    var payload = objectMapper.createObjectNode()
        .set("personIds", objectMapper.createArrayNode()
            .add(PersonDefaults.ID_STRING));

    var personIds = List.of(PersonDefaults.ID);
    when(service.updateEventParticipants(EventDefaults.ID, new EventParticipantsUpdateRequest(personIds)))
        .thenReturn(Flux.just(new PersonResponse(PersonDefaults.ID, PersonDefaults.NAME)));

    webTestClient.put().uri("/api/v1/events/{id}/participants", EventDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isArray()
        .jsonPath("$[0].id").isEqualTo(PersonDefaults.ID_STRING)
        .jsonPath("$[0].name").isEqualTo(PersonDefaults.NAME);
  }

  @Test
  void delete_event() {
    when(service.deleteEvent(EventDefaults.ID)).thenReturn(Mono.empty());

    webTestClient.delete().uri("/api/v1/events/{id}", EventDefaults.ID)
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