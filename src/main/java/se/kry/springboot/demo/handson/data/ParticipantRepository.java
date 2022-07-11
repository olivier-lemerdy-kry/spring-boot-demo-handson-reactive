package se.kry.springboot.demo.handson.data;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.util.UUID;
import org.springframework.data.repository.reactive.RxJava3SortingRepository;

public interface ParticipantRepository extends RxJava3SortingRepository<Participant, UUID> {

  Single<Long> deleteAllByEventId(UUID eventId);

  Flowable<Participant> findByEventId(UUID eventId);
}
