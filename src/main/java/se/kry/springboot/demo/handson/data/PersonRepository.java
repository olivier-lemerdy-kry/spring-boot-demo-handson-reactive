package se.kry.springboot.demo.handson.data;

import io.reactivex.Flowable;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.RxJava2SortingRepository;
import reactor.core.publisher.Flux;

public interface PersonRepository extends RxJava2SortingRepository<Person, UUID> {

  Flowable<Person> findBy(Pageable pageable);

  @Query("SELECT * FROM person JOIN participant ON participant.person_id = person.id WHERE participant.event_id = :eventId")
  Flowable<Person> findParticipantsByEventId(UUID eventId);
}
