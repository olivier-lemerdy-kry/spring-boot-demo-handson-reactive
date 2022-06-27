package se.kry.springboot.demo.handson.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;

class PersonServiceTest {

  interface Defaults {
    UUID ID = UUID.fromString("e1c47fc3-472d-4c14-8d7a-c1b4d1dbdfe5");

    String NAME = "Some name";

    Instant CREATED_DATE = Instant.EPOCH;

    Instant LAST_MODIFIED_DATE = Instant.EPOCH;
  }

  private PersonRepository repository;

  private PersonService service;

  @BeforeEach
  void setUp() {
    repository = mock(PersonRepository.class);
    service = new PersonService(repository);
  }

  @Test
  void update_person_with_null_id_fails() {
    var request = new PersonUpdateRequest(Optional.of("Some other name"));

    service.updatePerson(null, request)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_person_with_null_request_fails() {
    service.updatePerson(Defaults.ID, null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_person() {
    var request = new PersonUpdateRequest(Optional.of("Some other name"));

    when(repository.findById(Defaults.ID)).thenReturn(
        Mono.just(new Person(Defaults.ID, Defaults.NAME, Defaults.CREATED_DATE, Defaults.LAST_MODIFIED_DATE)));

    when(repository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0, Person.class)));

    service.updatePerson(Defaults.ID, request)
        .as(StepVerifier::create)
        .assertNext(personResponse -> {

        })
        .verifyComplete();
  }

  @Test
  void delete_person_with_null_id_fails() {
    service.deletePerson(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void delete_person() {
    when(repository.deleteById(Defaults.ID)).thenReturn(Mono.empty());

    service.deletePerson(Defaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}