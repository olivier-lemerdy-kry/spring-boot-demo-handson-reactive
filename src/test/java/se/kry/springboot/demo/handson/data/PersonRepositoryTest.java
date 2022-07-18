package se.kry.springboot.demo.handson.data;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@SpringBootTest // https://github.com/spring-projects/spring-boot/issues/23630
@Testcontainers
class PersonRepositoryTest {

  @Container
  private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:4.4");

  @Autowired
  private ReactiveNeo4jTemplate template;

  @Autowired
  private PersonRepository repository;

  @DynamicPropertySource
  static void mySqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
    registry.add("spring.neo4j.authentication.username", () -> "neo4j");
    registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
  }

  @Test
  void save_person() {
    repository.save(Person.from(PersonDefaults.NAME))
        .as(StepVerifier::create)
        .assertNext(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(PersonDefaults.NAME);
        })
        .verifyComplete();
  }

  @Test
  void find_participants_by_event_id() {
    // Given
    var eventId = UUID.randomUUID();
    var personId = UUID.randomUUID();

    template.save(Person.from(personId, PersonDefaults.NAME)).flatMap(person ->
            template.save(
                Event.from(eventId, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME,
                    singletonList(person))))

        // When
        .thenMany(repository.findParticipantsByEventId(eventId))

        // Then
        .as(StepVerifier::create)
        .assertNext(participant -> {
          assertThat(participant.id()).isEqualTo(personId);
          assertThat(participant.name()).isEqualTo(PersonDefaults.NAME);
        }).verifyComplete();
  }

}