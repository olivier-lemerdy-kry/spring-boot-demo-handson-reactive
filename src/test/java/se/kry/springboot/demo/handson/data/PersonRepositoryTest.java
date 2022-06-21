package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.test.StepVerifier;

@DataR2dbcTest
class PersonRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private PersonRepository repository;

  @Test
  void save_person() {
    repository.save(Person.from("John Doe"))
        .as(StepVerifier::create)
        .assertNext(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo("John Doe");
        })
        .verifyComplete();
  }

  @Test
  void save_person_with_too_long_name() {
    var name = "X".repeat(300);

    repository.save(Person.from(name))
        .as(StepVerifier::create)
        .expectErrorSatisfies(exception ->
            assertThat(exception)
                .isInstanceOf(UncategorizedDataAccessException.class)
                .hasMessageContaining("\"NAME CHARACTER VARYING(256)\": \"SPACE(300")
        ).verify();
  }

}