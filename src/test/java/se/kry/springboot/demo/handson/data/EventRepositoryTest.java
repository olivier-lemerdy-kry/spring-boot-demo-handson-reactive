package se.kry.springboot.demo.handson.data;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.dao.UncategorizedDataAccessException;
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

@DataNeo4jTest
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
  void get_event() {
    template.save(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                emptyList(),
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
    var saves = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i,
            EventDefaults.START_TIME.plusDays(i),
            EventDefaults.START_TIME.plusDays(i).plusHours(1)))
        .map(template::save)
        .collect(Collectors.toList());

    Mono.when(saves)
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

  @Test
  void save_event_with_too_long_title() {
    var title = "X".repeat(300);

    repository.save(Event.from(title, EventDefaults.START_TIME, EventDefaults.END_TIME))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception)
                .isInstanceOf(UncategorizedDataAccessException.class)
                .hasMessageContaining("\"TITLE CHARACTER VARYING(256)\": \"SPACE(300")
        ).verify();
  }

}