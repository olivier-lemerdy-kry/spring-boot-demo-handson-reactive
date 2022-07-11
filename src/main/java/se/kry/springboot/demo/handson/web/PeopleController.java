package se.kry.springboot.demo.handson.web;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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
import se.kry.springboot.demo.handson.domain.PersonCreationRequest;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;
import se.kry.springboot.demo.handson.services.PersonService;

@RestController
@RequestMapping("/api/v1/people")
public class PeopleController {

  private final PersonService service;

  public PeopleController(PersonService service) {
    this.service = service;
  }

  @PostMapping
  Single<ResponseEntity<PersonResponse>> createPerson(@Valid @RequestBody PersonCreationRequest personCreationRequest,
                                                      UriComponentsBuilder builder) {
    return service.createPerson(personCreationRequest).map(response -> {
      var location = builder.pathSegment("api", "v1", "people", "{id}").build(response.id());
      return ResponseEntity.created(location).body(response);
    });
  }

  @GetMapping
  Single<Page<PersonResponse>> readPeople(Pageable pageable) {
    return service.getPeople(pageable);
  }

  @GetMapping("{id}")
  Single<ResponseEntity<PersonResponse>> readPerson(@PathVariable UUID id) {
    return service.getPerson(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PatchMapping("{id}")
  Single<ResponseEntity<PersonResponse>> updatePerson(
      @PathVariable UUID id,
      @Valid @RequestBody PersonUpdateRequest request) {
    return service.updatePerson(id, request)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("{id}")
  Completable deletePerson(@PathVariable UUID id) {
    return service.deletePerson(id);
  }
}
