package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ParticipantRepository extends R2dbcRepository<Participant, UUID> {

  Mono<Void> deleteAllByEventId(UUID eventId);

  Flux<Participant> findByEventId(UUID eventId);
}
