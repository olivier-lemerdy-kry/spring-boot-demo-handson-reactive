package se.kry.springboot.demo.handson.data;

import static org.assertj.core.api.Assertions.assertThat;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import se.kry.springboot.demo.handson.domain.EventDefaults;
import se.kry.springboot.demo.handson.domain.PersonDefaults;

@DataR2dbcTest
class PersonRepositoryTest {

  @Autowired
  private R2dbcEntityTemplate template;

  @Autowired
  private PersonRepository repository;

  @Test
  void save_person() {
    repository.save(Person.from(PersonDefaults.NAME))
        .test()
        .assertValue(person -> {
          assertThat(person.id()).isNotNull();
          assertThat(person.name()).isEqualTo(PersonDefaults.NAME);
          return true;
        })
        .assertComplete();
  }

  @Test
  void save_person_with_too_long_name() {
    var name = "X".repeat(300);

    repository.save(Person.from(name))
        .test()
        .assertError(exception -> {
          assertThat(exception)
              .isInstanceOf(UncategorizedDataAccessException.class)
              .hasMessageContaining("\"NAME CHARACTER VARYING(256)\": \"SPACE(300");
          return true;
        });
  }

  @Test
  void find_participants_by_event_id() {
    monoToSingle(Mono.zip(
            template.insert(new Event(
                EventDefaults.ID, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME, null, null)),
            template.insert(new Person(PersonDefaults.ID, PersonDefaults.NAME, null, null)))
        .flatMap(tuple2 -> template.insert(Participant.from(EventDefaults.ID, PersonDefaults.ID))))
        .ignoreElement().andThen(repository.findParticipantsByEventId(EventDefaults.ID))
        .test()
        .assertValue(participant -> {
          assertThat(participant.id()).isEqualTo(PersonDefaults.ID);
          assertThat(participant.name()).isEqualTo(PersonDefaults.NAME);
          return true;
        }).assertComplete();
  }

}