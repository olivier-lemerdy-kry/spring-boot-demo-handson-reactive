package se.kry.springboot.demo.handson.data;


import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface EventRepository extends R2dbcRepository<Event, UUID> {
}
