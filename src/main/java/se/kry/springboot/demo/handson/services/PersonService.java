package se.kry.springboot.demo.handson.services;

import static se.kry.springboot.demo.handson.util.ReactivePreconditions.requireNonNull;

import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.PersonResponse;

@Service
public class PersonService {

  private final PersonRepository repository;

  public PersonService(PersonRepository repository) {
    this.repository = repository;
  }

  public Mono<Page<PersonResponse>> getPeople(@NotNull Pageable pageable) {
    return requireNonNull(pageable).flatMap(p ->
        Mono.zip(
            repository.count(),
            repository.findBy(pageable).collectList(),
            (count, list) -> new PageImpl<>(list, pageable, count).map(this::responseFromPerson)));
  }

  private PersonResponse responseFromPerson(Person person) {
    return new PersonResponse(person.id(), person.name());
  }
}
