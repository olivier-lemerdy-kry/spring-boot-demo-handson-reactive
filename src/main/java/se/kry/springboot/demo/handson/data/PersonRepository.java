package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PersonRepository extends ReactiveMongoRepository<Person, UUID> {

  Flux<Person> findBy(Pageable pageable);
}
