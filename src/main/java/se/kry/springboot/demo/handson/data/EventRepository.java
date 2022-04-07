package se.kry.springboot.demo.handson.data;


import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface EventRepository extends R2dbcRepository<Event, UUID> {

  Flux<Event> findAll(Pageable pageable);
}
