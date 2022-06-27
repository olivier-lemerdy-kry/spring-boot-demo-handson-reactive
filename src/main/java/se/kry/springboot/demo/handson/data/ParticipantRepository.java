package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ParticipantRepository extends R2dbcRepository<Participant, UUID> {

  Flux<Participant> findByEventId(UUID eventId);
}
