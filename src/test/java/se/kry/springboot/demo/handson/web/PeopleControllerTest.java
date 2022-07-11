package se.kry.springboot.demo.handson.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.kry.springboot.demo.handson.domain.PersonCreationRequest;
import se.kry.springboot.demo.handson.domain.PersonDefaults;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;
import se.kry.springboot.demo.handson.services.PersonService;

@WebFluxTest(PeopleController.class)
class PeopleControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PersonService service;

  @Test
  void create_person() {
    var payload = objectMapper.createObjectNode()
        .put("name", PersonDefaults.NAME)
        .toString();

    when(service.createPerson(new PersonCreationRequest(PersonDefaults.NAME)))
        .thenReturn(Single.just(new PersonResponse(PersonDefaults.ID, PersonDefaults.NAME)));

    webTestClient.post().uri("/api/v1/people")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().valueEquals("Location", "/api/v1/people/" + PersonDefaults.ID)
        .expectBody()
        .jsonPath("$.id").isEqualTo(PersonDefaults.ID_STRING)
        .jsonPath("$.name").isEqualTo(PersonDefaults.NAME);
  }

  @Test
  void create_person_with_blank_name() {
    var payload = objectMapper.createObjectNode()
        .put("name", " ")
        .toString();

    webTestClient.post().uri("/api/v1/people")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void create_person_with_too_long_name() {
    var payload = objectMapper.createObjectNode()
        .put("name", "X".repeat(300))
        .toString();

    webTestClient.post().uri("/api/v1/people")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void read_people() {
    var content = List.of(
        new PersonResponse(PersonDefaults.ID, PersonDefaults.NAME),
        new PersonResponse(PersonDefaults.OTHER_ID, PersonDefaults.OTHER_NAME)
    );

    var pageable = PageRequest.ofSize(20);

    when(service.getPeople(pageable))
        .thenReturn(Single.just(new PageImpl<>(content, pageable, content.size())));

    webTestClient.get().uri("/api/v1/people")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$").isMap()
        .jsonPath("$.content").isArray()
        .jsonPath("$.content[0].id").isEqualTo(PersonDefaults.ID_STRING)
        .jsonPath("$.content[0].name").isEqualTo(PersonDefaults.NAME)
        .jsonPath("$.content[1].id").isEqualTo(PersonDefaults.OTHER_ID_STRING)
        .jsonPath("$.content[1].name").isEqualTo(PersonDefaults.OTHER_NAME)
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
  void read_person() {
    when(service.getPerson(PersonDefaults.ID)).thenReturn(
        Maybe.just(new PersonResponse(PersonDefaults.ID, PersonDefaults.NAME)));

    webTestClient.get().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.name").isEqualTo(PersonDefaults.NAME);
  }

  @Test
  void read_person_with_unknown_id() {
    when(service.getPerson(PersonDefaults.ID)).thenReturn(Maybe.empty());

    webTestClient.get().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_person() {
    var payload = objectMapper.createObjectNode()
        .put("name", PersonDefaults.OTHER_NAME)
        .toString();

    when(service.updatePerson(PersonDefaults.ID, new PersonUpdateRequest(Optional.of(PersonDefaults.OTHER_NAME))))
        .thenReturn(Maybe.just(new PersonResponse(PersonDefaults.ID, PersonDefaults.OTHER_NAME)));

    webTestClient.patch().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.name").isEqualTo(PersonDefaults.OTHER_NAME);
  }

  @Test
  void update_person_with_incorrect_id() {
    var payload = objectMapper.createObjectNode()
        .put("name", PersonDefaults.OTHER_NAME)
        .toString();

    webTestClient.patch().uri("/api/v1/people/{id}", "foobar")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void update_person_with_unknown_id() {
    when(service.updatePerson(eq(PersonDefaults.ID), any()))
        .thenReturn(Maybe.empty());

    var payload = objectMapper.createObjectNode()
        .put("name", PersonDefaults.OTHER_NAME)
        .toString();

    webTestClient.patch().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void update_person_with_too_long_name() {
    var payload = objectMapper.createObjectNode()
        .put("name", "X".repeat(300))
        .toString();

    webTestClient.patch().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void delete_person() {
    when(service.deletePerson(PersonDefaults.ID)).thenReturn(Completable.complete());

    webTestClient.delete().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .exchange()
        .expectStatus().isOk()
        .expectBody().isEmpty();
  }

  @Test
  void delete_person_with_incorrect_id() {
    webTestClient.delete().uri("/api/v1/people/{id}", "foobar")
        .exchange()
        .expectStatus().isBadRequest();
  }
}
