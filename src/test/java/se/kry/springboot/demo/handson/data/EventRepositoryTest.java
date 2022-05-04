package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

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
    var end = start.plusHours(12);

    template.insert(new Event(id, "Some event", start, end, Instant.EPOCH, Instant.EPOCH)).block();

    var event = repository.findById(id);

    event.test()
        .assertValue(actual -> {
          assertThat(actual.id()).isEqualTo(id);
          assertThat(actual.title()).isEqualTo("Some event");
          assertThat(actual.start()).hasToString("2001-01-01T00:00");
          assertThat(actual.end()).hasToString("2001-01-01T12:00");
          return true;
        }).assertComplete();
  }

  @Test
  void get_events() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i, start.plusDays(i), start.plusDays(i).plusHours(12)))
        .map(template::insert)
        .collect(Collectors.toList());
    Mono.when(inserts).block();

    repository.findBy(Pageable.ofSize(20))
        .toList()
        .test()
        .assertValue(events -> {
          assertThat(events).hasSize(20);
          return true;
        }).assertComplete();
  }

  @Test
  void save_event() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    repository.save(Event.from("Some event", start, end))
        .test()
        .assertValue(event -> {
          assertThat(event).isNotNull();
          assertThat(event.id()).isNotNull();
          assertThat(event.title()).isEqualTo("Some event");
          assertThat(event.start()).hasToString("2001-01-01T00:00");
          assertThat(event.end()).hasToString("2001-01-01T12:00");
          return true;
        }).assertComplete();
  }

  @Test
  void save_event_with_too_long_title() {
    var title = "X".repeat(300);
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    repository.save(Event.from(title, start, end))
        .test()
        .assertError(exception -> {
          assertThat(exception)
              .isInstanceOf(UncategorizedDataAccessException.class)
              .hasMessageContaining("Value too long for column \"TITLE VARCHAR(256)\"");
          return true;
        });
  }

}