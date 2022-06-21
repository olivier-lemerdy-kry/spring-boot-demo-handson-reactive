package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface PersonRepository extends R2dbcRepository<Person, UUID> {

  Flux<Person> findBy(Pageable pageable);
}
