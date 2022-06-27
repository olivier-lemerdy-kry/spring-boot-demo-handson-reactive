package se.kry.springboot.demo.handson.data;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record Participant(@Id UUID id,
                          @NotNull UUID eventId,
                          @NotNull UUID personId,
                          @CreatedDate Instant createdDate) implements Persistable<UUID> {

  public static Participant from(@NotNull UUID eventId, @NotNull UUID personId) {
    return new Participant(UUID.randomUUID(), eventId, personId, null);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return createdDate == null;
  }
}
