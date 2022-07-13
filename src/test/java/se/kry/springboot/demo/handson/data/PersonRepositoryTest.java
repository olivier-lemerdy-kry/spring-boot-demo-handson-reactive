package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
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
        .test()
        .assertValue(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(PersonDefaults.NAME);
          return true;
        })
        .assertComplete();
  }

  @Test
  void save_person_with_too_long_name() {
    var name = "X".repeat(300);

    repository.save(Person.from(name))
        .test()
        .assertError(exception -> {
          assertThat(exception)
              .isInstanceOf(UncategorizedDataAccessException.class)
              .hasMessageContaining("\"NAME CHARACTER VARYING(256)\": \"SPACE(300");
          return true;
        });
  }

  @Test
  void find_participants_by_event_id() {
    // Given
    var eventId = UUID.randomUUID();
    var personId = UUID.randomUUID();

    monoToSingle(template.insert(
            Event.from(eventId, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME))
        .then(template.insert(Person.from(personId, PersonDefaults.NAME)))
        .then(template.insert(Participant.from(eventId, personId))))

        // When
        .ignoreElement().andThen(repository.findParticipantsByEventId(eventId))

        // Then
        .test()
        .assertValue(participant -> {
          assertThat(participant.id()).isEqualTo(personId);
          assertThat(participant.name()).isEqualTo(PersonDefaults.NAME);
          return true;
        }).assertComplete();
  }

}