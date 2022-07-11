package se.kry.springboot.demo.handson.data;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import java.util.UUID;
import org.springframework.data.repository.reactive.RxJava2SortingRepository;

public interface ParticipantRepository extends RxJava2SortingRepository<Participant, UUID> {

  Completable deleteAllByEventId(UUID eventId);

  Flowable<Participant> findByEventId(UUID eventId);
}
