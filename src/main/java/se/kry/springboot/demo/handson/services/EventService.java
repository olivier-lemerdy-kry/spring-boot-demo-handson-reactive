package se.kry.springboot.demo.handson.services;

import static se.kry.springboot.demo.handson.services.EventFunctions.newEventFromCreationRequest;
import static se.kry.springboot.demo.handson.services.EventFunctions.updateEventFromUpdateRequest;
import static se.kry.springboot.demo.handson.util.MonoPreconditions.requireNonNull;

import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.data.Participant;
import se.kry.springboot.demo.handson.data.ParticipantRepository;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventParticipantsUpdateRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.util.FluxPreconditions;

@Service
public class EventService {

  private final EventRepository eventRepository;

  private final ParticipantRepository participantRepository;

  private final PersonRepository personRepository;

  public EventService(EventRepository eventRepository,
                      ParticipantRepository participantRepository,
                      PersonRepository personRepository) {
    this.eventRepository = eventRepository;
    this.participantRepository = participantRepository;
    this.personRepository = personRepository;
  }

  @Transactional
  public Mono<EventResponse> createEvent(@NotNull EventCreationRequest eventCreationRequest) {
    return requireNonNull(eventCreationRequest).flatMap(p ->
        eventRepository.save(newEventFromCreationRequest(eventCreationRequest))
            .map(EventFunctions::responseFromEvent));
  }

  public Mono<Page<EventResponse>> getEvents(@NotNull Pageable pageable) {
    return requireNonNull(pageable).flatMap(p ->
        Mono.zip(
            eventRepository.count(),
            eventRepository.findBy(pageable).collectList(),
            (count, list) -> new PageImpl<>(list, pageable, count).map(EventFunctions::responseFromEvent)));
  }

  public Mono<EventResponse> getEvent(@NotNull UUID id) {
    return requireNonNull(id)
        .flatMap(p -> eventRepository.findById(id))
        .map(EventFunctions::responseFromEvent);
  }

  public Flux<PersonResponse> getEventParticipants(@NotNull UUID id) {
    return FluxPreconditions.requireNonNull(id).flatMap(p ->
        personRepository.findParticipantsByEventId(id)
            .map(PersonFunctions::responseFromPerson));
  }

  @Transactional
  public Mono<EventResponse> updateEvent(@NotNull UUID id, @NotNull EventUpdateRequest eventUpdateRequest) {
    return requireNonNull(id, eventUpdateRequest).flatMap(p ->
        eventRepository.findById(id)
            .map(event -> updateEventFromUpdateRequest(event, eventUpdateRequest))
            .flatMap(eventRepository::save)
            .map(EventFunctions::responseFromEvent));
  }

  @Transactional
  public Flux<PersonResponse> updateEventParticipants(UUID eventId, EventParticipantsUpdateRequest request) {
    return FluxPreconditions.requireNonNull(eventId, request).flatMap(p ->
        participantRepository.deleteAllByEventId(eventId)
            .thenMany(participantRepository.saveAll(
                Flux.fromStream(request.personIds().stream().map(personId -> Participant.from(eventId, personId)))))
            .thenMany(personRepository.findParticipantsByEventId(eventId)
                .map(PersonFunctions::responseFromPerson)));
  }

  @Transactional
  public Mono<Void> deleteEvent(@NotNull UUID id) {
    return requireNonNull(id).flatMap(p ->
        eventRepository.deleteById(id));
  }
}
