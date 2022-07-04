package se.kry.springboot.demo.handson.web;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
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
  void update_people() {
    var payload = objectMapper.createObjectNode()
        .put("name", PersonDefaults.OTHER_NAME)
        .toString();

    when(service.updatePerson(PersonDefaults.ID, new PersonUpdateRequest(Optional.of(PersonDefaults.OTHER_NAME))))
        .thenReturn(Mono.just(new PersonResponse(PersonDefaults.ID, PersonDefaults.OTHER_NAME)));

    webTestClient.patch().uri("/api/v1/people/{id}", PersonDefaults.ID)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(payload)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.name").isEqualTo(PersonDefaults.OTHER_NAME);
  }

  @Test
  void delete_people() {
    when(service.deletePerson(PersonDefaults.ID)).thenReturn(Mono.empty());

    webTestClient.delete().uri("/api/v1/people/{id}", PersonDefaults.ID)
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
