package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface PersonRepository extends R2dbcRepository<Person, UUID> {

  Flux<Person> findBy(Pageable pageable);

  @Query("SELECT * FROM person JOIN participant ON participant.person_id = person.id WHERE participant.event_id = :eventId")
  Flux<Person> findParticipantsByEventId(UUID eventId);
}
