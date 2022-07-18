package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;
import static reactor.adapter.rxjava.RxJava3Adapter.monoToCompletable;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.domain.EventDefaults;

@DataR2dbcTest
class EventRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private EventRepository repository;

  @Test
  void find_event_by_id() {

    // Given
    monoToCompletable(
    template.insert(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE)))

        // When
        .andThen(repository.findById(EventDefaults.ID))

        // Then
        .test()
        .assertValue(actual -> {
          assertThat(actual.id()).isEqualTo(EventDefaults.ID);
          assertThat(actual.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(actual.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(actual.endTime()).isEqualTo(EventDefaults.END_TIME);
          return true;
        }).assertComplete();
  }

  @Test
  void find_all_events_by_pageable() {

    // Given
    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i,
            EventDefaults.START_TIME.plusDays(i),
            EventDefaults.START_TIME.plusDays(i).plusHours(1)))
        .map(template::insert)
        .collect(Collectors.toList());
    Mono.when(inserts).block();

    // When
    repository.findBy(Pageable.ofSize(20))
        .toList()

        // Then
        .test()
        .assertValue(events -> {
          assertThat(events).hasSize(20);
          return true;
        }).assertComplete();
  }

  @Test
  void save_event() {

    // Given
    var event = Event.from(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME);

    // When
    repository.save(event)

        // Then
        .test()
        .assertValue(actual -> {
          assertThat(actual).isNotNull();
          assertThat(actual.id()).isNotNull();
          assertThat(actual.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(actual.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(actual.endTime()).isEqualTo(EventDefaults.END_TIME);
          return true;
        }).assertComplete();
  }

  @Test
  void save_event_with_too_long_title() {
    var title = "X".repeat(300);

    repository.save(Event.from(title, EventDefaults.START_TIME, EventDefaults.END_TIME))
        .test()
        .assertError(exception -> {
          assertThat(exception)
              .isInstanceOf(UncategorizedDataAccessException.class)
              .hasMessageContaining("\"TITLE CHARACTER VARYING(256)\": \"SPACE(300");
          return true;
        });
  }

}