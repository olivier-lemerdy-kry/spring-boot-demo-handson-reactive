package se.kry.springboot.demo.handson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;

class EventServiceTest {

  private EventService service;

  private EventRepository repository;

  @BeforeEach
  void setup() {
    repository = mock(EventRepository.class);
    service = new EventService(repository);
  }

  @Test
  void create_event() {
    var startTime = LocalDate.of(2001, Month.JANUARY, 1).atTime(12, 0);
    var endTime = startTime.plusHours(1);
    var creationRequest = new EventCreationRequest("foobar", startTime, endTime);

    var idReference = new AtomicReference<UUID>();
    when(repository.save(any())).thenAnswer((Answer<Mono<Event>>) invocation -> {
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
          assertThat(eventResponse.title()).isEqualTo("foobar");
          assertThat(eventResponse.startTime()).isEqualTo(startTime);
          assertThat(eventResponse.endTime()).isEqualTo(endTime);
        }).verifyComplete();
  }

}