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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;

class EventServiceTest {

  public static final UUID DEFAULT_ID = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
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
    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());

    service.getEvent(DEFAULT_ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void get_event() {
    when(repository.findById(DEFAULT_ID)).thenReturn(
        Mono.just(new Event(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_TIME, DEFAULT_END_TIME, Instant.EPOCH, Instant.EPOCH)));

    service.getEvent(DEFAULT_ID)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(DEFAULT_ID);
          assertThat(eventResponse.title()).isEqualTo(DEFAULT_TITLE);
          assertThat(eventResponse.startTime()).isEqualTo(DEFAULT_START_TIME);
          assertThat(eventResponse.endTime()).isEqualTo(DEFAULT_END_TIME);
        })
        .verifyComplete();
  }

  @Test
  void update_event_with_null_id_fails() {
    var request =
        new EventUpdateRequest(Optional.of(DEFAULT_TITLE), Optional.of(DEFAULT_START_TIME), Optional.of(DEFAULT_END_TIME));

    service.updateEvent(null, request)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_with_null_update_request_fails() {
    service.updateEvent(DEFAULT_ID, null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void update_event_not_found() {
    var request =
        new EventUpdateRequest(Optional.of(DEFAULT_TITLE), Optional.of(DEFAULT_START_TIME), Optional.of(DEFAULT_END_TIME));

    when(repository.findById(DEFAULT_ID)).thenReturn(Mono.empty());

    service.updateEvent(DEFAULT_ID, request)
        .as(StepVerifier::create)
        .verifyComplete();
  }

  @Test
  void update_event() {
    var anotherStartTime = LocalDate.of(2002, Month.FEBRUARY, 2).atTime(14, 0);
    var anotherEndTime = anotherStartTime.plusHours(2);

    var request =
        new EventUpdateRequest(Optional.of("Another title"), Optional.of(anotherStartTime), Optional.of(anotherEndTime));

    when(repository.findById(DEFAULT_ID)).thenReturn(
        Mono.just(new Event(DEFAULT_ID, DEFAULT_TITLE, DEFAULT_START_TIME, DEFAULT_END_TIME, Instant.EPOCH, Instant.EPOCH)));

    when(repository.save(any())).thenAnswer(invocation ->
        Mono.just(invocation.getArgument(0, Event.class)));

    service.updateEvent(DEFAULT_ID, request)
        .as(StepVerifier::create)
        .assertNext(eventResponse -> {
          assertThat(eventResponse.id()).isEqualTo(DEFAULT_ID);
          assertThat(eventResponse.title()).isEqualTo("Another title");
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
    when(repository.deleteById(DEFAULT_ID)).thenReturn(Mono.empty());

    service.deleteEvent(DEFAULT_ID)
        .as(StepVerifier::create)
        .verifyComplete();
  }

}