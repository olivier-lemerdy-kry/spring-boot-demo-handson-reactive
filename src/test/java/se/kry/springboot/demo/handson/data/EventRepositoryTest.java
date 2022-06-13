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
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    template.save(new Event(id, "Some event", start, end, Instant.EPOCH, Instant.EPOCH)).block();

    var event = repository.findById(id);

    event.as(StepVerifier::create).assertNext(actual -> {
      assertThat(actual.id()).isEqualTo(id);
      assertThat(actual.title()).isEqualTo("Some event");
      assertThat(actual.startTime()).hasToString("2001-01-01T00:00");
      assertThat(actual.endTime()).hasToString("2001-01-01T12:00");
    }).verifyComplete();
  }

  @Test
  void get_events() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);

    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Event.from("Event" + i, start.plusDays(i), start.plusDays(i).plusHours(12)))
        .map(template::save)
        .collect(Collectors.toList());
    Mono.when(inserts).block();

    repository.findBy(Pageable.ofSize(20))
        .collectList()
        .as(StepVerifier::create)
        .assertNext(events ->
            assertThat(events).hasSize(20)
        ).verifyComplete();
  }

  @Test
  void save_event() {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    repository.save(Event.from("Some event", start, end))
        .as(StepVerifier::create)
        .assertNext(event -> {
          assertThat(event).isNotNull();
          assertThat(event.id()).isNotNull();
          assertThat(event.title()).isEqualTo("Some event");
          assertThat(event.startTime()).hasToString("2001-01-01T00:00");
          assertThat(event.endTime()).hasToString("2001-01-01T12:00");
        }).verifyComplete();
  }

  @Test
  void save_event_with_too_long_title() {
    var title = "X".repeat(300);
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    repository.save(Event.from(title, start, end))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception)
                .isInstanceOf(UncategorizedDataAccessException.class)
                .hasMessageContaining("\"TITLE CHARACTER VARYING(256)\": \"SPACE(300")
        ).verify();
  }

}