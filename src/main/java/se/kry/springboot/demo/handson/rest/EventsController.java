package se.kry.springboot.demo.handson.rest;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
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
  Single<ResponseEntity<EventResponse>> createEvent(@Valid @RequestBody EventCreationRequest eventCreationRequest,
                                                    UriComponentsBuilder builder) {
    return service.createEvent(eventCreationRequest).map(response -> {
      var location = builder.pathSegment("api", "v1", "events", "{id}").build(response.id());
      return ResponseEntity.created(location).body(response);
    });
  }

  @GetMapping
  Single<Page<EventResponse>> readEvents(Pageable pageable) {
    return service.getEvents(pageable);
  }

  @GetMapping("{id}")
  Maybe<ResponseEntity<EventResponse>> readEvent(@PathVariable UUID id) {
    return service.getEvent(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PatchMapping("{id}")
  Maybe<ResponseEntity<EventResponse>> updateEvent(
      @PathVariable UUID id,
      @Valid @RequestBody EventUpdateRequest eventUpdateRequest) {
    return service.updateEvent(id, eventUpdateRequest)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("{id}")
  Completable deleteEvent(@PathVariable UUID id) {
    return service.deleteEvent(id);
  }
}
