package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
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
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.data.ParticipantRepository;
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
        .test()
        .assertError(NullPointerException.class);
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
      return Single.just(
          new Event(
              inputEvent.id(), inputEvent.title(), inputEvent.startTime(), inputEvent.endTime(),
              EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE));
    });

    service.createEvent(creationRequest)
        .test()
        .assertValue(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(idReference.get());
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.END_TIME);
          return true;
        }).assertComplete();
  }

  @Test
  void get_event_with_null_id_fails() {
    service.getEvent(null)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void get_event_not_found() {
    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Maybe.empty());

    service.getEvent(EventDefaults.ID)
        .test()
        .assertComplete();
  }

  @Test
  void get_event() {
    when(eventRepository.findById(EventDefaults.ID))
        .thenReturn(Maybe.just(
            new Event(EventDefaults.ID, EventDefaults.TITLE,
                EventDefaults.START_TIME, EventDefaults.END_TIME,
                EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE)));

    service.getEvent(EventDefaults.ID)
        .test()
        .assertValue(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(EventDefaults.ID);
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.END_TIME);
          return true;
        }).assertComplete();
  }

  @Test
  void get_events_with_null_pageable_fails() {
    service.getEvents(null)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void get_events() {
    var pageable = Pageable.ofSize(5);

    when(eventRepository.count()).thenReturn(Single.just(99L));

    when(eventRepository.findBy(pageable)).thenAnswer(invocation -> {
      var invokedPageable = invocation.getArgument(0, Pageable.class);
      return Flowable.fromStream(IntStream.range(0, invokedPageable.getPageSize()).mapToObj(i ->
          Event.from("Event " + i, EventDefaults.START_TIME.plusDays(i),
              EventDefaults.START_TIME.plusDays(i).plusHours(i + 1))));
    });

    service.getEvents(pageable)
        .test()
        .assertValue(page -> {
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
          return true;
        }).assertComplete();
  }

  @Test
  void update_event_with_null_id_fails() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.TITLE),
        Optional.of(EventDefaults.START_TIME),
        Optional.of(EventDefaults.END_TIME));

    service.updateEvent(null, request)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void update_event_with_null_update_request_fails() {
    service.updateEvent(EventDefaults.ID, null)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void update_event_not_found() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.TITLE),
        Optional.of(EventDefaults.START_TIME),
        Optional.of(EventDefaults.END_TIME));

    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Maybe.empty());

    service.updateEvent(EventDefaults.ID, request)
        .test()
        .assertComplete();
  }

  @Test
  void update_event() {
    var request = new EventUpdateRequest(
        Optional.of(EventDefaults.OTHER_TITLE),
        Optional.of(EventDefaults.OTHER_START_TIME),
        Optional.of(EventDefaults.OTHER_END_TIME));

    when(eventRepository.findById(EventDefaults.ID)).thenReturn(Maybe.just(
        new Event(
            EventDefaults.ID, EventDefaults.TITLE,
            EventDefaults.START_TIME, EventDefaults.END_TIME,
            EventDefaults.CREATED_DATE, EventDefaults.LAST_MODIFIED_DATE)));

    when(eventRepository.save(any())).thenAnswer(invocation ->
        Single.just(invocation.getArgument(0, Event.class)));

    service.updateEvent(EventDefaults.ID, request)
        .test()
        .assertValue(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(EventDefaults.ID);
          assertThat(eventResponse.title()).isEqualTo(EventDefaults.OTHER_TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(EventDefaults.OTHER_START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(EventDefaults.OTHER_END_TIME);
          return true;
        }).assertComplete();
  }

  @Test
  void update_event_participants() {
    var request = new EventParticipantsUpdateRequest(List.of(PersonDefaults.ID, PersonDefaults.OTHER_ID));

    when(participantRepository.deleteAllByEventId(EventDefaults.ID)).thenReturn(Single.just(1L));

    when(participantRepository.saveAll(any(Flowable.class))).thenAnswer(invocation ->
        invocation.getArgument(0, Flowable.class));

    when(personRepository.findParticipantsByEventId(EventDefaults.ID)).thenReturn(
        Flowable.just(
            new Person(PersonDefaults.ID, PersonDefaults.NAME,
                PersonDefaults.CREATED_DATE, PersonDefaults.LAST_MODIFIED_DATE),
            new Person(PersonDefaults.OTHER_ID, PersonDefaults.OTHER_NAME,
                PersonDefaults.CREATED_DATE, PersonDefaults.LAST_MODIFIED_DATE)));

    service.updateEventParticipants(EventDefaults.ID, request)
        .test()
        .assertValueCount(2)
        .assertValueAt(0, personResponse -> {
          assertThat(personResponse.id()).isEqualTo(PersonDefaults.ID);
          assertThat(personResponse.name()).isEqualTo(PersonDefaults.NAME);
          return true;
        })
        .assertValueAt(1, personResponse -> {
          assertThat(personResponse.id()).isEqualTo(PersonDefaults.OTHER_ID);
          assertThat(personResponse.name()).isEqualTo(PersonDefaults.OTHER_NAME);
          return true;
        })
        .assertComplete();
  }

  @Test
  void delete_event_with_null_id_fails() {
    service.deleteEvent(null)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void delete_event() {
    when(eventRepository.deleteById(EventDefaults.ID)).thenReturn(Completable.complete());

    service.deleteEvent(EventDefaults.ID)
        .test()
        .assertComplete();
  }

}