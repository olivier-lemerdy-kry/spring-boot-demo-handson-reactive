package se.kry.springboot.demo.handson.services;

import javax.validation.constraints.NotNull;
import se.kry.springboot.demo.handson.data.Event;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
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

  static Event updateEventFromUpdateRequest(@NotNull Event event, @NotNull EventUpdateRequest eventUpdateRequest) {
    return event.copy(
        title -> eventUpdateRequest.title().orElse(title),
        start -> eventUpdateRequest.startTime().orElse(start),
        end -> eventUpdateRequest.endTime().orElse(end)
    );
  }

  static EventResponse responseFromEvent(Event event) {
    return new EventResponse(event.id(), event.title(), event.startTime(), event.endTime());
  }
}
