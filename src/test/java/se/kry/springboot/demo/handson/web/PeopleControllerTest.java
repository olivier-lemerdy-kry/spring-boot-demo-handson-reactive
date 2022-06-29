package se.kry.springboot.demo.handson.web;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;
import se.kry.springboot.demo.handson.services.PersonService;

@WebFluxTest(PeopleController.class)
class PeopleControllerTest {

  interface Defaults {
    UUID ID = UUID.fromString("e1c47fc3-472d-4c14-8d7a-c1b4d1dbdfe5");
  }

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PersonService service;

  @Test
  void update_people() {
    var payload = objectMapper.createObjectNode()
        .put("name", "Jane Doe")
        .toString();

    when(service.updatePerson(Defaults.ID, new PersonUpdateRequest(Optional.of("Jane Doe"))))
        .thenReturn(Mono.just(new PersonResponse(Defaults.ID, "Jane Doe")));

    webTestClient.patch().uri("/api/v1/people/{id}", Defaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.name").isEqualTo("Jane Doe");
  }

  @Test
  void delete_people() {
    when(service.deletePerson(Defaults.ID)).thenReturn(Mono.empty());

    webTestClient.delete().uri("/api/v1/people/{id}", Defaults.ID)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void delete_people_with_incorrect_id() {
    webTestClient.delete().uri("/api/v1/people/{id}", "foobar")
        .exchange()
        .expectStatus().isBadRequest();
  }
}
