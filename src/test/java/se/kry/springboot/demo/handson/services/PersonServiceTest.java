package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.PersonDefaults;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;

class PersonServiceTest {

  private PersonRepository repository;

  private PersonService service;

  @BeforeEach
  void setUp() {
    repository = mock(PersonRepository.class);
    service = new PersonService(repository);
  }

  @Test
  void update_person_with_null_id_fails() {
    var request = new PersonUpdateRequest(Optional.of(PersonDefaults.OTHER_NAME));

    service.updatePerson(null, request)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_person_with_null_request_fails() {
    service.updatePerson(PersonDefaults.ID, null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_person() {
    var request = new PersonUpdateRequest(Optional.of(PersonDefaults.OTHER_NAME));

    when(repository.findById(PersonDefaults.ID)).thenReturn(
        Mono.just(new Person(PersonDefaults.ID, PersonDefaults.NAME, PersonDefaults.CREATED_DATE,
            PersonDefaults.LAST_MODIFIED_DATE)));

    when(repository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0, Person.class)));

    service.updatePerson(PersonDefaults.ID, request)
        .as(StepVerifier::create)
        .assertNext(personResponse -> {
          assertThat(personResponse.id()).isEqualTo(PersonDefaults.ID);
          assertThat(personResponse.name()).isEqualTo(PersonDefaults.OTHER_NAME);
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
    when(repository.deleteById(PersonDefaults.ID)).thenReturn(Mono.empty());

    service.deletePerson(PersonDefaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}