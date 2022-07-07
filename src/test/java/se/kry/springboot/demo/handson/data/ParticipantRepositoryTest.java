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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@DataR2dbcTest
class ParticipantRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private ParticipantRepository repository;

  @Test
  void find_by_event_id() {
    Mono.zip(
            template.insert(Event.from(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME)),
            template.insert(Person.from(PersonDefaults.NAME))
        ).flatMapMany(tuple -> {
          var eventId = tuple.getT1().id();
          var personId = tuple.getT2().id();

          return template.insert(Participant.from(eventId, personId))
              .thenMany(repository.findByEventId(eventId))
              .map(participant -> Tuples.of(eventId, personId, participant));
        })
        .as(StepVerifier::create)
        .assertNext(tuple -> {
          var eventId = tuple.getT1();
          var personId = tuple.getT2();
          var participant = tuple.getT3();

          assertThat(participant.id()).isNotNull();
          assertThat(participant.eventId()).isEqualTo(eventId);
          assertThat(participant.personId()).isEqualTo(personId);
        }).verifyComplete();
    ;
  }

  @Test
  void save_all() {
    Mono.zip(
            template.insert(Event.from("Event 1", EventDefaults.START_TIME, EventDefaults.END_TIME)),
            template.insert(Event.from("Event 2", EventDefaults.START_TIME, EventDefaults.END_TIME)),
            template.insert(Person.from("Person 1")),
            template.insert(Person.from("Person 2")))
        .flatMap(tuple4 -> {
          var event1Id = tuple4.getT1().id();
          var event2Id = tuple4.getT2().id();
          var person1Id = tuple4.getT3().id();
          var person2Id = tuple4.getT4().id();

          return repository.saveAll(Flux.just(
                  Participant.from(event1Id, person1Id),
                  Participant.from(event2Id, person1Id),
                  Participant.from(event1Id, person2Id),
                  Participant.from(event2Id, person2Id)))
              .collectList();
        }).as(StepVerifier::create)
        .assertNext(participants -> {
          assertThat(participants).hasSize(4);
        }).verifyComplete();
  }

}