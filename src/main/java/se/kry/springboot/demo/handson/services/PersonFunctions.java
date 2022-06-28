package se.kry.springboot.demo.handson.services;

import javax.validation.constraints.NotNull;
import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.domain.PersonCreationRequest;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;

enum PersonFunctions {
  ;

  static Person newPersonFromCreationRequest(@NotNull PersonCreationRequest personCreationRequest) {
    return Person.from(personCreationRequest.name());
  }

  static PersonResponse responseFromPerson(Person person) {
    return new PersonResponse(person.id(), person.name());
  }

  static Person updatePersonFromUpdateRequest(Person person, PersonUpdateRequest personUpdateRequest) {
    return person.copy(
        name -> personUpdateRequest.name().orElse(name)
    );
  }
}
