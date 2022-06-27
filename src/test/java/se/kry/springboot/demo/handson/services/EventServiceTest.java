package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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
import se.kry.springboot.demo.handson.data.ParticipantRepository;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;

class EventServiceTest {

  interface Defaults {
    UUID ID = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    String TITLE = "Some event";

    LocalDateTime START_TIME = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);

    LocalDateTime END_TIME = START_TIME.plusHours(1);

    Instant CREATED_DATE = Instant.EPOCH;

    Instant LAST_MODIFIED_DATE = Instant.EPOCH;
  }

  private EventService service;

  private EventRepository eventRepository;

  private ParticipantRepository participantRepository;

  private PersonRepository personRepository;

  @BeforeEach
  void setup() {
    eventRepository = mock(EventRepository.class);
    participantRepository = mock(ParticipantRepository.class);
    personRepository = mock(PersonRepository.class);
    service = new EventService(eventRepository, participantRepository, personRepository);
  }

  @Test
  void create_event_with_null_event_fails() {
    service.createEvent(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void create_event() {
    var creationRequest = new EventCreationRequest(Defaults.TITLE, Defaults.START_TIME, Defaults.END_TIME);

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
              Defaults.CREATED_DATE, Defaults.LAST_MODIFIED_DATE));
    });

    service.createEvent(creationRequest)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(idReference.get());
          assertThat(eventResponse.title()).isEqualTo(Defaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(Defaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(Defaults.END_TIME);
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
    when(eventRepository.findById(Defaults.ID)).thenReturn(Mono.empty());

    service.getEvent(Defaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void get_event() {
    when(eventRepository.findById(Defaults.ID))
        .thenReturn(Mono.just(
            new Event(Defaults.ID, Defaults.TITLE, Defaults.START_TIME, Defaults.END_TIME, Defaults.CREATED_DATE,
                Defaults.LAST_MODIFIED_DATE)));

    service.getEvent(Defaults.ID)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(Defaults.ID);
          assertThat(eventResponse.title()).isEqualTo(Defaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(Defaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(Defaults.END_TIME);
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
    var baseTime = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);

    when(eventRepository.count()).thenReturn(Mono.just(99L));

    when(eventRepository.findBy(pageable)).thenAnswer(invocation -> {
      var invokedPageable = invocation.getArgument(0, Pageable.class);
      return Flux.fromStream(IntStream.range(0, invokedPageable.getPageSize()).mapToObj(i ->
          Event.from("Event " + i, baseTime.plusDays(i), baseTime.plusDays(i).plusHours(i + 1))));
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
        Optional.of(Defaults.TITLE),
        Optional.of(Defaults.START_TIME),
        Optional.of(Defaults.END_TIME));

    service.updateEvent(null, request)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_with_null_update_request_fails() {
    service.updateEvent(Defaults.ID, null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_not_found() {
    var request = new EventUpdateRequest(
        Optional.of(Defaults.TITLE),
        Optional.of(Defaults.START_TIME),
        Optional.of(Defaults.END_TIME));

    when(eventRepository.findById(Defaults.ID)).thenReturn(Mono.empty());

    service.updateEvent(Defaults.ID, request)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void update_event() {
    var anotherStartTime = LocalDate.of(2002, Month.FEBRUARY, 2).atTime(14, 0);
    var anotherEndTime = anotherStartTime.plusHours(2);

    var request =
        new EventUpdateRequest(Optional.of("Some other event"), Optional.of(anotherStartTime), Optional.of(anotherEndTime));

    when(eventRepository.findById(Defaults.ID)).thenReturn(
        Mono.just(
            new Event(Defaults.ID, Defaults.TITLE, Defaults.START_TIME, Defaults.END_TIME, Defaults.CREATED_DATE,
                Defaults.LAST_MODIFIED_DATE)));

    when(eventRepository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0, Event.class)));

    service.updateEvent(Defaults.ID, request)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(Defaults.ID);
          assertThat(eventResponse.title()).isEqualTo("Some other event");
          assertThat(eventResponse.startTime()).isEqualTo(anotherStartTime);
          assertThat(eventResponse.endTime()).isEqualTo(anotherEndTime);
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
    when(eventRepository.deleteById(Defaults.ID)).thenReturn(Mono.empty());

    service.deleteEvent(Defaults.ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}