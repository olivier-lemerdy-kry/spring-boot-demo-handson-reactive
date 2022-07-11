package se.kry.springboot.demo.handson.data;

import io.reactivex.rxjava3.core.Flowable;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.RxJava3SortingRepository;

public interface PersonRepository extends RxJava3SortingRepository<Person, UUID> {

  Flowable<Person> findBy(Pageable pageable);

  @Query("SELECT * FROM person JOIN participant ON participant.person_id = person.id WHERE participant.event_id = :eventId")
  Flowable<Person> findParticipantsByEventId(UUID eventId);
}
