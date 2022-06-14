package se.kry.springboot.demo.handson.services;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;

@Service
public class EventService {

  private final EventRepository repository;

  public EventService(EventRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public Mono<EventResponse> createEvent(@NotNull EventCreationRequest eventCreationRequest) {
    return requireNonNull(eventCreationRequest).flatMap(p ->
        repository.save(newEventFromCreationRequest(eventCreationRequest))
            .map(this::responseFromEvent));
  }

  public Mono<Page<EventResponse>> getEvents(@NotNull Pageable pageable) {
    return requireNonNull(pageable).flatMap(p ->
        Mono.zip(
            repository.count(),
            repository.findBy(pageable).collectList(),
            (count, list) -> new PageImpl<>(list, pageable, count).map(this::responseFromEvent)));
  }

  public Mono<EventResponse> getEvent(@NotNull UUID id) {
    return requireNonNull(id).flatMap(p ->
        repository.findById(id)
            .map(this::responseFromEvent));
  }

  @Transactional
  public Mono<EventResponse> updateEvent(@NotNull UUID id, @NotNull EventUpdateRequest eventUpdateRequest) {
    return repository.findById(id)
        .map(event -> updateEventFromUpdateRequest(event, eventUpdateRequest))
        .flatMap(repository::save)
        .map(this::responseFromEvent);
  }

  @Transactional
  public Mono<Void> deleteEvent(@NotNull UUID id) {
    return requireNonNull(id).flatMap(p ->
        repository.deleteById(id));
  }

  private Event newEventFromCreationRequest(@NotNull EventCreationRequest eventCreationRequest) {
    return Event.from(
        eventCreationRequest.title(),
        eventCreationRequest.startTime(),
        eventCreationRequest.endTime());
  }

  private Event updateEventFromUpdateRequest(@NotNull Event event, @NotNull EventUpdateRequest eventUpdateRequest) {
    return event.copy(
        title -> eventUpdateRequest.title().orElse(title),
        start -> eventUpdateRequest.startTime().orElse(start),
        end -> eventUpdateRequest.endTime().orElse(end)
    );
  }

  private EventResponse responseFromEvent(Event event) {
    return new EventResponse(event.id(), event.title(), event.startTime(), event.endTime());
  }

  private <T> Mono<T> requireNonNull(T someObject) {
    if (someObject == null) {
      return Mono.error(NullPointerException::new);
    }
    return Mono.just(someObject);
  }
}
