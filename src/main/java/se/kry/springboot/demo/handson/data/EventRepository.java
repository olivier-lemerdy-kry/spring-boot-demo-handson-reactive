package se.kry.springboot.demo.handson.data;

import io.reactivex.rxjava3.core.Flowable;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.RxJava3SortingRepository;

public interface EventRepository extends RxJava3SortingRepository<Event, UUID> {

  Flowable<Event> findBy(Pageable pageable);
}
