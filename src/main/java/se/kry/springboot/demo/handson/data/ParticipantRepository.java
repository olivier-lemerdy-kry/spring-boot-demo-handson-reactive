package se.kry.springboot.demo.handson.data;

import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.UUID;
import org.springframework.data.repository.reactive.RxJava2SortingRepository;

public interface ParticipantRepository extends RxJava2SortingRepository<Participant, UUID> {

  Single<Long> deleteAllByEventId(UUID eventId);

  Flowable<Participant> findByEventId(UUID eventId);
}
