package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@DataR2dbcTest
class PersonRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private PersonRepository repository;

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
  void save_person_with_too_long_name() {
    var name = "X".repeat(300);

    repository.save(Person.from(name))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception)
                .isInstanceOf(UncategorizedDataAccessException.class)
                .hasMessageContaining("\"NAME CHARACTER VARYING(256)\": \"SPACE(300")
        ).verify();
  }

  @Test
  void find_all_people_by_pageable() {

    // Given
    var inserts = IntStream.range(0, 50)
        .mapToObj(i -> Person.from(PersonDefaults.NAME + ' ' + i))
        .map(template::insert)
        .toList();

    Mono.when(inserts)

        // When
        .then(repository.findBy(Pageable.ofSize(20)).collectList())

        // Then
        .as(StepVerifier::create)
        .assertNext(people ->
            assertThat(people).hasSize(20)
        ).verifyComplete();
  }

  @Test
  void find_participants_by_event_id() {
    // Given
    var eventId = UUID.randomUUID();
    var personId = UUID.randomUUID();

    template.insert(
            Event.from(eventId, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME))
        .then(template.insert(Person.from(personId, PersonDefaults.NAME)))
        .then(template.insert(Participant.from(eventId, personId)))

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