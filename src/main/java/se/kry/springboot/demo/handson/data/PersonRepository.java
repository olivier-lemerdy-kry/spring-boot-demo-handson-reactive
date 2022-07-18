package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;

public interface PersonRepository extends ReactiveNeo4jRepository<Person, UUID> {

  Flux<Person> findBy(Pageable pageable);

  @Query("MATCH (p:Person)-[r]->(e:Event) WHERE e.id=$eventId RETURN p")
  Flux<Person> findParticipantsByEventId(UUID eventId);
}
