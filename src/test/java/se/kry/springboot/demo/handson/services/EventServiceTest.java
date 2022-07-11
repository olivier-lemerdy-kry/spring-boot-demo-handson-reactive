package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.EventParticipantsUpdateRequest;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

class EventServiceTest {

  private EventService service;

  private EventRepository eventRepository;

  private PersonRepository personRepository;

  @BeforeEach
  void setup() {
    eventRepository = mock(EventRepository.class);
    personRepository = mock(PersonRepository.class);
    service = new EventService(eventRepository, personRepository);
  }

  @Test
  void create_event_with_null_event_fails() {
    service.createEvent(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void create_event() {
    var creationRequest = new EventCreationRequest(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME);

    var idReference = new AtomicReference<UUID>();
    when(eventRepository.save(any())).thenAnswer(invocation -> {
      var inputEvent = invocation.getArgument(0, Event.class);
      assertThat(inputEvent.id()).isNotNull();
      assertThat(inputEvent.createdDate()).isNull();
      assertThat(inputEvent.lastModifiedDate()).isNull();
      idReference.set(inputEvent.id());
      return Mono.just(
          new Event(
              inputEvent.id(), inputEvent.title(), inputEvent.startTime(), inputEvent.endTime(),
              EventDefaults.PARTICIPANT_IDS, EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE));
    });

    service.createEvent(creationRequest)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(idReference.get());
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.END_TIME);
        }).verifyComplete();
  }

  @Test
  void get_event_with_null_id_fails() {
    service.getEvent(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void get_event_not_found() {
    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Mono.empty());

    service.getEvent(EventDefaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void get_event() {
    when(eventRepository.findById(EventDefaults.ID))
        .thenReturn(Mono.just(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                EventDefaults.PARTICIPANT_IDS,
                EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE)));

    service.getEvent(EventDefaults.ID)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(EventDefaults.ID);
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.END_TIME);
        })
        .verifyComplete();
  }

  @Test
  void get_events_with_null_pageable_fails() {
    service.getEvents(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void get_events() {
    var pageable = Pageable.ofSize(5);

    when(eventRepository.count()).thenReturn(Mono.just(99L));

    when(eventRepository.findBy(pageable)).thenAnswer(invocation -> {
      var invokedPageable = invocation.getArgument(0, Pageable.class);
      return Flux.fromStream(IntStream.range(0, invokedPageable.getPageSize()).mapToObj(i ->
          Event.from("Event " + i, EventDefaults.START_TIME.plusDays(i),
              EventDefaults.START_TIME.plusDays(i).plusHours(i + 1))));
    });

    service.getEvents(pageable)
        .as(StepVerifier::create)
        .assertNext(page -> {
          assertThat(page.getTotalElements()).isEqualTo(99L);
          assertThat(page.getTotalPages()).isEqualTo(20);
          assertThat(page.getNumber()).isZero();

          assertThat(page.getContent())
              .hasSize(5)
              .satisfies(eventResponse -> {
                assertThat(eventResponse.title()).isEqualTo("Event 0");
                assertThat(eventResponse.startTime()).isEqualTo(LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0));
                assertThat(eventResponse.endTime()).isEqualTo(LocalDate.of(2001, Month.JANUARY, 1).atTime(13, 0));
              }, Index.atIndex(0))
              .satisfies(eventResponse -> {
                assertThat(eventResponse.title()).isEqualTo("Event 4");
                assertThat(eventResponse.startTime()).isEqualTo(LocalDate.of(2001, Month.JANUARY, 5).atTime(12, 0));
                assertThat(eventResponse.endTime()).isEqualTo(LocalDate.of(2001, Month.JANUARY, 5).atTime(17, 0));
              }, Index.atIndex(4));
        })
        .verifyComplete();
  }

  @Test
  void update_event_with_null_id_fails() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.TITLE),
        Optional.of(EventDefaults.START_TIME),
        Optional.of(EventDefaults.END_TIME));

    service.updateEvent(null, request)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_with_null_update_request_fails() {
    service.updateEvent(EventDefaults.ID, null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_not_found() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.TITLE),
        Optional.of(EventDefaults.START_TIME),
        Optional.of(EventDefaults.END_TIME));

    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Mono.empty());

    service.updateEvent(EventDefaults.ID, request)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void update_event() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.OTHER_TITLE),
        Optional.of(EventDefaults.OTHER_START_TIME),
        Optional.of(EventDefaults.OTHER_END_TIME));

    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Mono.just(
        new Event(
            EventDefaults.ID, EventDefaults.TITLE,
            EventDefaults.START_TIME, EventDefaults.END_TIME,
            EventDefaults.PARTICIPANT_IDS,
            EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE)));

    when(eventRepository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0, Event.class)));

    service.updateEvent(EventDefaults.ID, request)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(EventDefaults.ID);
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.OTHER_TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.OTHER_START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.OTHER_END_TIME);
        })
        .verifyComplete();
  }

  @Test
  void update_event_participants() {
    var participantIds = List.of(PersonDefaults.ID, PersonDefaults.OTHER_ID);
    var request = new EventParticipantsUpdateRequest(participantIds);

    var initialEvent = new Event(
        EventDefaults.ID, EventDefaults.TITLE,
        EventDefaults.START_TIME, EventDefaults.END_TIME,
        EventDefaults.PARTICIPANT_IDS,
        EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE);

    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Mono.just(initialEvent));

    var updatedEvent = new Event(
        EventDefaults.ID, EventDefaults.TITLE,
        EventDefaults.START_TIME, EventDefaults.END_TIME,
        participantIds,
        EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE);

    when(eventRepository.save(updatedEvent)).thenReturn(Mono.just(updatedEvent));

    when(personRepository.findAllById(participantIds)).thenReturn(Flux.just(
        new Person(PersonDefaults.ID, PersonDefaults.NAME, PersonDefaults.CREATED_DATE, PersonDefaults.LAST_MODIFIED_DATE),
        new Person(PersonDefaults.OTHER_ID, PersonDefaults.OTHER_NAME, PersonDefaults.CREATED_DATE,
            PersonDefaults.LAST_MODIFIED_DATE)
    ));

    service.updateEventParticipants(EventDefaults.ID, request)
        .as(StepVerifier::create)
        .assertNext(personResponse -> {
          assertThat(personResponse.id()).isEqualTo(PersonDefaults.ID);
          assertThat(personResponse.name()).isEqualTo(PersonDefaults.NAME);
        })
        .assertNext(personResponse -> {
          assertThat(personResponse.id()).isEqualTo(PersonDefaults.OTHER_ID);
          assertThat(personResponse.name()).isEqualTo(PersonDefaults.OTHER_NAME);
        })
        .verifyComplete();
  }

  @Test
  void delete_event_with_null_id_fails() {
    service.deleteEvent(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void delete_event() {
    when(eventRepository.deleteById(EventDefaults.ID)).thenReturn(Mono.empty());

    service.deleteEvent(EventDefaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}