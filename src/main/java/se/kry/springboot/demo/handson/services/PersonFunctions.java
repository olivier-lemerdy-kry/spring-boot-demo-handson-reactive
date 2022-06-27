package se.kry.springboot.demo.handson.services;

import se.kry.springboot.demo.handson.data.Person;
import se.kry.springboot.demo.handson.domain.PersonResponse;
import se.kry.springboot.demo.handson.domain.PersonUpdateRequest;

enum PersonFunctions {
  ;

  static PersonResponse responseFromPerson(Person person) {
    return new PersonResponse(person.id(), person.name());
  }

  static Person updatePersonFromUpdateRequest(Person person, PersonUpdateRequest personUpdateRequest) {
    return person.copy(
        name -> personUpdateRequest.name().orElse(name)
    );
  }
}
