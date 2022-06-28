package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

@DataR2dbcTest
class PersonRepositoryTest {

  interface Defaults {
    interface Person {
      String NAME = "John Doe";
    }

    interface Event {
      String TITLE = "Some event";

      LocalDateTime START_TIME = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);

      LocalDateTime END_TIME = START_TIME.plusHours(1);
    }
  }

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private PersonRepository repository;

  @Test
  void save_person() {
    repository.save(Person.from(Defaults.Person.NAME))
        .as(StepVerifier::create)
        .assertNext(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(Defaults.Person.NAME);
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
    var timeout = Duration.ofSeconds(2);
    var event = template.insert(Event.from(
            ParticipantRepositoryTest.Defaults.Event.TITLE, ParticipantRepositoryTest.Defaults.Event.START, ParticipantRepositoryTest.Defaults.Event.END))
        .block(timeout);
    var person = template.insert(Person.from(ParticipantRepositoryTest.Defaults.Person.NAME))
        .block(timeout);
    template.insert(Participant.from(event.id(), person.id()))
        .block(timeout);

    repository.findParticipantsByEventId(event.id())
        .as(StepVerifier::create)
        .assertNext(participant -> {
          assertThat(participant.id()).isEqualTo(person.id());
          assertThat(participant.name()).isEqualTo(Defaults.Person.NAME);
        }).verifyComplete();
  }

}