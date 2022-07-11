package se.kry.springboot.demo.handson.services;

import javax.validation.constraints.NotNull;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventParticipantsUpdateRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;

public enum EventFunctions {
  ;

  static Event newEventFromCreationRequest(@NotNull EventCreationRequest eventCreationRequest) {
    return Event.from(
        eventCreationRequest.title(),
        eventCreationRequest.startTime(),
        eventCreationRequest.endTime());
  }

  static Event updateEventFromUpdateRequest(@NotNull Event event, @NotNull EventUpdateRequest updateRequest) {
    return event.copy(
        title -> updateRequest.title().orElse(title),
        start -> updateRequest.startTime().orElse(start),
        end -> updateRequest.endTime().orElse(end)
    );
  }

  static Event updateEventParticipantsFromUpdateRequest(@NotNull Event event,
                                                        @NotNull EventParticipantsUpdateRequest updateRequest) {
    return event.copy(participantIds -> updateRequest.personIds());
  }

  static EventResponse responseFromEvent(Event event) {
    return new EventResponse(event.id(), event.title(), event.startTime(), event.endTime());
  }
}
