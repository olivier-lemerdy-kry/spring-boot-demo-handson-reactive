package se.kry.springboot.demo.handson.data;


import io.reactivex.Flowable;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.RxJava2SortingRepository;

public interface EventRepository extends RxJava2SortingRepository<Event, UUID> {

  Flowable<Event> findBy(Pageable pageable);
}
