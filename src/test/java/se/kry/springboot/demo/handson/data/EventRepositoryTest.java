package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
class EventRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private EventRepository repository;

  @Test
  void get_event() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    template.insert(new Event(id, "Some event", start, start.plusHours(12))).block();

    var event = repository.findById(id);

    event.as(StepVerifier::create).assertNext(actual -> {
      assertThat(actual.id()).isEqualTo(id);
      assertThat(actual.title()).isEqualTo("Some event");
      assertThat(actual.start()).hasToString("2001-01-01T00:00");
      assertThat(actual.end()).hasToString("2001-01-01T12:00");
    }).verifyComplete();
  }

  @Test
  void get_events() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i, start.plusDays(i), start.plusDays(i).plusHours(12)))
        .map(template::insert)
        .collect(Collectors.toList());
    Mono.when(inserts).block();

    repository.findAll(Pageable.ofSize(20))
        .collectList()
        .as(StepVerifier::create)
        .assertNext(events ->
            assertThat(events).hasSize(20)
        ).verifyComplete();
  }

  @Test
  void save_event() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    repository.save(Event.from("Some event", start, start.plusHours(12)))
        .as(StepVerifier::create)
        .assertNext(event -> {
          assertThat(event).isNotNull();
          assertThat(event.id()).isNotNull();
          assertFalse(event.isNew());
          assertThat(event.title()).isEqualTo("Some event");
          assertThat(event.start()).hasToString("2001-01-01T00:00");
          assertThat(event.end()).hasToString("2001-01-01T12:00");
        }).verifyComplete();
  }

  @Test
  void save_event_with_blank_title() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    repository.save(Event.from(" ", start, start.plusHours(12)))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception).isInstanceOf(ConstraintViolationException.class)
        ).verify();
  }

  @Test
  void save_event_with_too_long_title() {
    var title = "X".repeat(300);
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    repository.save(Event.from(title, start, start.plusHours(12)))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception).isInstanceOf(ConstraintViolationException.class)
        ).verify();
  }

}