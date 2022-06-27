package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
class ParticipantRepositoryTest {

  interface Defaults {
    String PERSON_NAME = "Some person";

    String EVENT_TITLE = "Some event";

    LocalDateTime EVENT_START = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);

    LocalDateTime EVENT_END = EVENT_START.plusHours(1);
  }

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private ParticipantRepository repository;

  @Test
  void find_by_event_id() {
    var timeout = Duration.ofSeconds(2);

    var event = template.insert(Event.from(Defaults.EVENT_TITLE, Defaults.EVENT_START, Defaults.EVENT_END))
        .block(timeout);
    var person = template.insert(Person.from(Defaults.PERSON_NAME))
        .block(timeout);
    template.insert(Participant.from(event.id(), person.id()))
        .block(timeout);

    repository.findByEventId(event.id())
        .as(StepVerifier::create)
        .assertNext(participant -> {
          assertThat(participant.id()).isNotNull();
          assertThat(participant.eventId()).isEqualTo(event.id());
          assertThat(participant.personId()).isEqualTo(person.id());
        }).verifyComplete();
  }

}