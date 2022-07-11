package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.EventDefaults;

@DataMongoTest
class EventRepositoryTest {

  @Autowired
  private ReactiveMongoTemplate template;

  @Autowired
  private EventRepository repository;

  @Test
  void get_event() {
    template.insert(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE))
        .then(repository.findById(EventDefaults.ID))
        .as(StepVerifier::create).assertNext(actual -> {
          assertThat(actual.id()).isEqualTo(EventDefaults.ID);
          assertThat(actual.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(actual.startTime()).hasToString("2001-01-01T12:00");
          assertThat(actual.endTime()).hasToString("2001-01-01T13:00");
        }).verifyComplete();
  }

  @Test
  void get_events() {
    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i,
            EventDefaults.START_TIME.plusDays(i),
            EventDefaults.START_TIME.plusDays(i).plusHours(1)))
        .map(template::insert)
        .collect(Collectors.toList());

    Mono.when(inserts)
        .then(repository.findBy(Pageable.ofSize(20)).collectList())
        .as(StepVerifier::create)
        .assertNext(events ->
            assertThat(events).hasSize(20)
        ).verifyComplete();
  }

  @Test
  void save_event() {
    var event = Event.from(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME);
    repository.save(event)
        .as(StepVerifier::create)
        .assertNext(actual -> {
          assertThat(actual).isNotNull();
          assertThat(actual.id()).isNotNull();
          assertThat(actual.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(actual.startTime()).hasToString("2001-01-01T12:00");
          assertThat(actual.endTime()).hasToString("2001-01-01T13:00");
        }).verifyComplete();
  }

}