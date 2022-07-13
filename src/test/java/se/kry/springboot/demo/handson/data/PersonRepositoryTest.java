package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@DataR2dbcTest
class PersonRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private PersonRepository repository;

  @Test
  void save_person() {
    repository.save(Person.from(PersonDefaults.NAME))
        .as(StepVerifier::create)
        .assertNext(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(PersonDefaults.NAME);
        })
        .verifyComplete();
  }

  @Test
  void save_person_with_too_long_name() {
    var name = "X".repeat(300);

    repository.save(Person.from(name))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception)
                .isInstanceOf(UncategorizedDataAccessException.class)
                .hasMessageContaining("\"NAME CHARACTER VARYING(256)\": \"SPACE(300")
        ).verify();
  }

  @Test
  void find_participants_by_event_id() {
    Mono.zip(
            template.insert(new Event(
                EventDefaults.ID, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME, null, null)),
            template.insert(new Person(PersonDefaults.ID, PersonDefaults.NAME, null, null)))
        .flatMap(tuple2 -> {
          return template.insert(Participant.from(EventDefaults.ID, PersonDefaults.ID));
        })
        .flatMapMany(participant -> repository.findParticipantsByEventId(EventDefaults.ID))
        .as(StepVerifier::create)
        .assertNext(participant -> {
          assertThat(participant.id()).isEqualTo(PersonDefaults.ID);
          assertThat(participant.name()).isEqualTo(PersonDefaults.NAME);
        }).verifyComplete();
  }

}