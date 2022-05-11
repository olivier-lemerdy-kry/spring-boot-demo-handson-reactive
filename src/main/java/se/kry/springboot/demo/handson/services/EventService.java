package se.kry.springboot.demo.handson.services;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
  public Single<EventResponse> createEvent(@NotNull EventCreationRequest eventCreationRequest) {
    return repository.save(newEventFromCreationRequest(eventCreationRequest))
        .map(this::responseFromEvent);
  }

  public Single<Page<EventResponse>> getEvents(@NotNull Pageable pageable) {
    return Single.zip(
        repository.count(),
        repository.findBy(pageable).toList(),
        (count, list) -> new PageImpl<>(list, pageable, count).map(this::responseFromEvent));
  }

  public Maybe<EventResponse> getEvent(@NotNull UUID id) {
    return repository.findById(id)
        .map(this::responseFromEvent);
  }

  @Transactional
  public Maybe<EventResponse> updateEvent(@NotNull UUID id, @NotNull EventUpdateRequest eventUpdateRequest) {
    return repository.findById(id)
        .map(event -> updateEventFromUpdateRequest(event, eventUpdateRequest))
        .flatMapSingle(repository::save)
        .map(this::responseFromEvent);
  }

  @Transactional
  public Completable deleteEvent(@NotNull UUID id) {
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

  private EventResponse responseFromEvent(Event event) {
    return new EventResponse(event.id(), event.title(), event.start(), event.end());
  }
}
