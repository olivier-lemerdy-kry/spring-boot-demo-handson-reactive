package se.kry.springboot.demo.handson.data;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.EventDefaults;

@SpringBootTest // https://github.com/spring-projects/spring-boot/issues/23630
@Testcontainers
class EventRepositoryTest {

  @Container
  private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:4.4");

  @Autowired
  private ReactiveNeo4jTemplate template;

  @Autowired
  private EventRepository repository;

  @DynamicPropertySource
  static void mySqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
  }

  @Test
  void find_event_by_id() {

    // Given
    template.save(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                emptyList(),
                EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE))

        // When
        .then(repository.findById(EventDefaults.ID))

        // Then
        .as(StepVerifier::create).assertNext(actual -> {
          assertThat(actual.id()).isEqualTo(EventDefaults.ID);
          assertThat(actual.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(actual.startTime()).hasToString("2001-01-01T12:00");
          assertThat(actual.endTime()).hasToString("2001-01-01T13:00");
        }).verifyComplete();
  }

  @Test
  void find_all_events_by_pageable() {

    // Given
    var saves = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i,
            EventDefaults.START_TIME.plusDays(i),
            EventDefaults.START_TIME.plusDays(i).plusHours(1)))
        .map(template::save)
        .toList();

    Mono.when(saves)

        // When
        .then(repository.findBy(Pageable.ofSize(20)).collectList())

        // Then
        .as(StepVerifier::create)
        .assertNext(events ->
            assertThat(events).hasSize(20)
        ).verifyComplete();
  }

  @Test
  void save_event() {

    // Given
    var event = Event.from(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME);

    // When
    repository.save(event)

        // Then
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