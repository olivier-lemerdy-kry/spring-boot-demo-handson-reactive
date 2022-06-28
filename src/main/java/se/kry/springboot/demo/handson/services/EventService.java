package se.kry.springboot.demo.handson.services;

import static java.util.function.Predicate.not;
import static se.kry.springboot.demo.handson.services.EventFunctions.newEventFromCreationRequest;
import static se.kry.springboot.demo.handson.services.EventFunctions.updateEventFromUpdateRequest;
import static se.kry.springboot.demo.handson.util.MonoPreconditions.requireNonNull;

import java.util.List;
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

  public Flux<PersonResponse> updateEventParticipants(UUID eventId, EventParticipantsUpdateRequest request) {
    return FluxPreconditions.requireNonNull(eventId, request).flatMap(p ->
        participantRepository.findByEventId(eventId).collectList()
            .flatMap(currentParticipants ->
                Mono.zip(
                    deleteObsoleteParticipants(request, currentParticipants),
                    saveNewParticipants(eventId, request, currentParticipants))).thenMany(
                personRepository.findParticipantsByEventId(eventId)
                    .map(PersonFunctions::responseFromPerson)));
  }

  private Mono<Void> deleteObsoleteParticipants(EventParticipantsUpdateRequest request,
                                                List<Participant> currentParticipants) {
    return participantRepository.deleteById(
        Flux.fromStream(currentParticipants.stream()
                .filter(not(participant -> request.participantIds().contains(participant.personId()))))
            .map(Participant::id));
  }

  private Mono<List<Participant>> saveNewParticipants(UUID eventId, EventParticipantsUpdateRequest request,
                                                      List<Participant> currentParticipants) {
    return participantRepository.saveAll(
        Flux.fromStream(
            request.participantIds().stream().filter(
                    not(personId -> currentParticipants.stream().map(Participant::personId)
                        .anyMatch(participantPersonId -> participantPersonId.equals(personId))))
                .map(newPersonId -> Participant.from(eventId, newPersonId)))).collectList();
  }

  @Transactional
  public Mono<Void> deleteEvent(@NotNull UUID id) {
    return requireNonNull(id).flatMap(p ->
        eventRepository.deleteById(id));
  }
}
