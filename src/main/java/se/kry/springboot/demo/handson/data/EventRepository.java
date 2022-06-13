package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import reactor.core.publisher.Flux;

public interface EventRepository extends ReactiveNeo4jRepository<Event, UUID> {

  Flux<Event> findBy(Pageable pageable);
}
