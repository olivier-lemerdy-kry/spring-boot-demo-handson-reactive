package se.kry.springboot.demo.handson.services;

import static se.kry.springboot.demo.handson.services.PersonFunctions.newPersonFromCreationRequest;
import static se.kry.springboot.demo.handson.services.PersonFunctions.updatePersonFromUpdateRequest;
import static se.kry.springboot.demo.handson.util.ReactivePreconditions.requireNonNull;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kry.springboot.demo.handson.data.PersonRepository;
import se.kry.springboot.demo.handson.domain.PersonCreationRequest;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;
import se.kry.springboot.demo.handson.util.ReactivePreconditions;

@Service
public class PersonService {

  private final PersonRepository repository;

  public PersonService(PersonRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public Single<PersonResponse> createPerson(@NotNull PersonCreationRequest personCreationRequest) {
    return requireNonNull(personCreationRequest).flatMap(p ->
        repository.save(newPersonFromCreationRequest(personCreationRequest))
            .map(PersonFunctions::responseFromPerson));
  }

  public Single<Page<PersonResponse>> getPeople(@NotNull Pageable pageable) {
    return requireNonNull(pageable).flatMap(p ->
        Single.zip(
            repository.count(),
            repository.findBy(pageable).toList(),
            (count, list) -> new PageImpl<>(list, pageable, count).map(PersonFunctions::responseFromPerson)));
  }

  public Maybe<PersonResponse> getPerson(@NotNull UUID id) {
    return requireNonNull(id)
        .flatMapMaybe(p -> repository.findById(id))
        .map(PersonFunctions::responseFromPerson);
  }

  @Transactional
  public Maybe<PersonResponse> updatePerson(@NotNull UUID id, @NotNull PersonUpdateRequest personUpdateRequest) {
    return requireNonNull(id, personUpdateRequest).flatMapMaybe(p ->
        repository.findById(id)
            .map(person -> updatePersonFromUpdateRequest(person, personUpdateRequest))
            .flatMapSingle(repository::save)
            .map(PersonFunctions::responseFromPerson));
  }

  @Transactional
  public Completable deletePerson(@NotNull UUID id) {
    return ReactivePreconditions.requireNonNull(id).flatMapCompletable(p -> repository.deleteById(id));
  }
}
