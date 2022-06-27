package se.kry.springboot.demo.handson.web;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
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

  @GetMapping
  Mono<Page<PersonResponse>> readPeople(Pageable pageable) {
    return service.getPeople(pageable);
  }

  @GetMapping("{id}")
  Mono<PersonResponse> readPerson(@PathVariable UUID id) {
    return service.getPerson(id);
  }

  @PatchMapping("{id}")
  Mono<PersonResponse> updatePerson(@PathVariable UUID id, PersonUpdateRequest request) {
    return service.updatePerson(id, request);
  }

  @DeleteMapping("{id}")
  Mono<Void> deletePerson(@PathVariable UUID id) {
    return service.deletePerson(id);
  }
}
