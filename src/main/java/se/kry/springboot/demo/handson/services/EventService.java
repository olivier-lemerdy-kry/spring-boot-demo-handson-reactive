package se.kry.springboot.demo.handson.services;

import static se.kry.springboot.demo.handson.services.EventFunctions.newEventFromCreationRequest;
import static se.kry.springboot.demo.handson.services.EventFunctions.updateEventFromUpdateRequest;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kry.springboot.demo.handson.data.EventRepository;
import se.kry.springboot.demo.handson.data.Participant;
import se.kry.springboot.demo.handson.data.ParticipantRepository;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventParticipantsUpdateRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.util.FlowablePreconditions;
import se.kry.springboot.demo.handson.util.MaybePreconditions;
import se.kry.springboot.demo.handson.util.SinglePreconditions;

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
  public Single<EventResponse> createEvent(@NotNull EventCreationRequest eventCreationRequest) {
    return SinglePreconditions.requireNonNull(eventCreationRequest).flatMap(p ->
        eventRepository.save(newEventFromCreationRequest(eventCreationRequest))
            .map(EventFunctions::responseFromEvent));
  }

  public Single<Page<EventResponse>> getEvents(@NotNull Pageable pageable) {
    return SinglePreconditions.requireNonNull(pageable).flatMap(p ->
        Single.zip(
            eventRepository.count(),
            eventRepository.findBy(pageable).toList(),
            (count, list) -> new PageImpl<>(list, pageable, count).map(EventFunctions::responseFromEvent)));
  }

  public Maybe<EventResponse> getEvent(@NotNull UUID id) {
    return MaybePreconditions.requireNonNull(id)
        .flatMap(p -> eventRepository.findById(id))
        .map(EventFunctions::responseFromEvent);
  }

  public Flowable<PersonResponse> getEventParticipants(@NotNull UUID id) {
    return FlowablePreconditions.requireNonNull(id).flatMap(p ->
        personRepository.findParticipantsByEventId(id)
            .map(PersonFunctions::responseFromPerson));
  }

  @Transactional
  public Maybe<EventResponse> updateEvent(@NotNull UUID id, @NotNull EventUpdateRequest eventUpdateRequest) {
    return MaybePreconditions.requireNonNull(id, eventUpdateRequest).flatMap(p ->
        eventRepository.findById(id)
            .map(event -> updateEventFromUpdateRequest(event, eventUpdateRequest))
            .flatMapSingle(eventRepository::save)
            .map(EventFunctions::responseFromEvent));
  }

  @Transactional
  public Flowable<PersonResponse> updateEventParticipants(UUID eventId, EventParticipantsUpdateRequest request) {
    return FlowablePreconditions.requireNonNull(eventId, request).flatMap(p ->
        participantRepository.deleteAllByEventId(eventId)
            .andThen(participantRepository.saveAll(
                Flowable.fromStream(request.personIds().stream().map(personId -> Participant.from(eventId, personId)))))
            .flatMap(q -> personRepository.findParticipantsByEventId(eventId)
                .map(PersonFunctions::responseFromPerson)));
  }

  @Transactional
  public Completable deleteEvent(@NotNull UUID id) {
    return SinglePreconditions.requireNonNull(id).flatMapCompletable(p ->
        eventRepository.deleteById(id));
  }
}
