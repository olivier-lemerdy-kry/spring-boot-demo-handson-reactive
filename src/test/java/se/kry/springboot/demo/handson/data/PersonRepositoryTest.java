package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@DataMongoTest
class PersonRepositoryTest {

  @Autowired
  private PersonRepository repository;

  @Test
  void save_person() {
    repository.save(Person.from(PersonDefaults.NAME))
        .as(StepVerifier::create)
        .assertNext(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(PersonDefaults.NAME);
        })
        .verifyComplete();
  }

}