package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.reactivex.Flowable;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.test.StepVerifier;
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
    // Given
    var eventId = UUID.randomUUID();
    var personId = UUID.randomUUID();

    template.insert(
            Event.from(eventId, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME))
        .then(template.insert(Person.from(personId, PersonDefaults.NAME)))
        .then(template.insert(Participant.from(eventId, personId)))

        // When
        .thenMany(repository.findByEventId(eventId))

        // Then
        .as(StepVerifier::create)
        .assertNext(participant -> {
          assertThat(participant.id()).isNotNull();
          assertThat(participant.eventId()).isEqualTo(eventId);
          assertThat(participant.personId()).isEqualTo(personId);
        }).verifyComplete();
  }

  @Test
  void save_all() {
    // Given
    var event1Id = UUID.randomUUID();
    var event2Id = UUID.randomUUID();
    var person1Id = UUID.randomUUID();
    var person2Id = UUID.randomUUID();

    RxJava2Adapter.monoToSingle(template.insert(
                Event.from(event1Id, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME))
            .then(template.insert(
                Event.from(event2Id, EventDefaults.OTHER_TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME)))
            .then(template.insert(Person.from(person1Id, PersonDefaults.NAME)))
            .then(template.insert(Person.from(person2Id, PersonDefaults.OTHER_NAME))))

        // When
        .ignoreElement().andThen(repository.saveAll(Flowable.just(
                Participant.from(event1Id, person1Id),
                Participant.from(event2Id, person1Id),
                Participant.from(event1Id, person2Id),
                Participant.from(event2Id, person2Id)))
            .toList())

        // Then
        .test()
        .assertValue(participants -> {
          assertThat(participants).hasSize(4);
          return true;
        }).assertComplete();
  }

}