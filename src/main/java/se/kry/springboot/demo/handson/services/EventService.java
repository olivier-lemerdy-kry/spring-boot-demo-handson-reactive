package se.kry.springboot.demo.handson.services;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;

@Service
public class EventService {

  private final EventRepository repository;

  public EventService(EventRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public Mono<Event> createEvent(@NotNull EventCreationRequest eventCreationRequest) {
    return repository.save(newEventFromCreationRequest(eventCreationRequest));
  }

  public Flux<Event> getEvents(@NotNull Pageable pageable) {
    return repository.findAll(pageable);
  }

  public Mono<Event> getEvent(@NotNull UUID id) {
    return repository.findById(id);
  }

  public Mono<Event> updateEvent(@NotNull UUID id, @NotNull EventUpdateRequest eventUpdateRequest) {
    return getEvent(id)
        .map(event -> updateEventFromUpdateRequest(event, eventUpdateRequest))
        .flatMap(repository::save);
  }

  public Mono<Void> deleteEvent(@NotNull UUID id) {
    return repository.deleteById(id);
  }

  private Event newEventFromCreationRequest(@NotNull EventCreationRequest eventCreationRequest) {
    return Event.from(
        eventCreationRequest.title(),
        eventCreationRequest.start(),
        eventCreationRequest.end());
  }

  private Event updateEventFromUpdateRequest(@NotNull Event event, @NotNull EventUpdateRequest eventUpdateRequest) {
    return event.copy(
        title -> eventUpdateRequest.title().orElse(title),
        start -> eventUpdateRequest.start().orElse(start),
        end -> eventUpdateRequest.end().orElse(end)
    );
  }
}
