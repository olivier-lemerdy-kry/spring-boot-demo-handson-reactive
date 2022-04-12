package se.kry.springboot.demo.handson.rest;

import java.util.UUID;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.domain.EventCreationRequest;
import se.kry.springboot.demo.handson.domain.EventResponse;
import se.kry.springboot.demo.handson.domain.EventUpdateRequest;
import se.kry.springboot.demo.handson.services.EventService;

@RestController
@RequestMapping("/api/v1/events")
public class EventsController {

  private final EventService service;

  public EventsController(EventService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Mono<EventResponse> createEvent(@Valid @RequestBody EventCreationRequest eventCreationRequest) {
    return service.createEvent(eventCreationRequest);
  }

  @GetMapping
  Mono<Page<EventResponse>> readEvents(Pageable pageable) {
    return service.getEvents(pageable);
  }

  @GetMapping("{id}")
  Mono<EventResponse> readEvent(@PathVariable UUID id) {
    return service.getEvent(id)
        .switchIfEmpty(Mono.error(EventNotFoundException::new));
  }

  @PatchMapping("{id}")
  Mono<EventResponse> updateEvent(@PathVariable UUID id,
                                  @Valid @RequestBody EventUpdateRequest eventUpdateRequest) {
    return service.updateEvent(id, eventUpdateRequest)
        .switchIfEmpty(Mono.error(EventNotFoundException::new));
  }

  @DeleteMapping("{id}")
  Mono<Void> deleteEvent(@PathVariable UUID id) {
    return service.deleteEvent(id);
  }
}
