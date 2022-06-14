package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;

class EventServiceTest {

  private static final String DEFAULT_TITLE = "My Event";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);
  public static final LocalDateTime DEFAULT_END_TIME = DEFAULT_START_TIME.plusHours(1);

  private EventService service;

  private EventRepository repository;

  @BeforeEach
  void setup() {
    repository = mock(EventRepository.class);
    service = new EventService(repository);
  }

  @Test
  void create_event_with_null_event_fails() {
    service.createEvent(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void create_event() {
    var creationRequest = new EventCreationRequest(DEFAULT_TITLE, DEFAULT_START_TIME, DEFAULT_END_TIME);

    var idReference = new AtomicReference<UUID>();
    when(repository.save(any())).thenAnswer(invocation -> {
      var inputEvent = invocation.getArgument(0, Event.class);
      assertThat(inputEvent.id()).isNotNull();
      assertThat(inputEvent.createdDate()).isNull();
      assertThat(inputEvent.lastModifiedDate()).isNull();
      idReference.set(inputEvent.id());
      return Mono.just(
          new Event(
              inputEvent.id(),
              inputEvent.title(),
              inputEvent.startTime(),
              inputEvent.endTime(),
              Instant.EPOCH,
              Instant.EPOCH));
    });

    service.createEvent(creationRequest)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(idReference.get());
          assertThat(eventResponse.title()).isEqualTo(DEFAULT_TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(DEFAULT_START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(DEFAULT_END_TIME);
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
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    when(repository.findById(id)).thenReturn(Mono.empty());

    service.getEvent(id)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void get_event() {
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    when(repository.findById(id)).thenReturn(
        Mono.just(new Event(id, DEFAULT_TITLE, DEFAULT_START_TIME, DEFAULT_END_TIME, Instant.EPOCH, Instant.EPOCH)));

    service.getEvent(id)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(id);
          assertThat(eventResponse.title()).isEqualTo(DEFAULT_TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(DEFAULT_START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(DEFAULT_END_TIME);
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
    var id = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    when(repository.deleteById(id)).thenReturn(Mono.empty());

    service.deleteEvent(id)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}