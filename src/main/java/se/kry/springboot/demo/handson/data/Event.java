package se.kry.springboot.demo.handson.data;

import static java.util.Collections.emptyList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import se.kry.springboot.demo.handson.domain.EventConstants;

@Node
public record Event(@Id UUID id,
                    @NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                    @NotNull LocalDateTime startTime,
                    @NotNull LocalDateTime endTime,
                    @NotNull @Relationship(type = Relationships.PARTICIPATES, direction = Relationship.Direction.INCOMING) List<Person> participants,
                    @CreatedDate Instant createdDate,
                    @LastModifiedDate Instant lastModifiedDate) implements Persistable<UUID> {

  interface Relationships {
    String PARTICIPATES = "PARTICIPATES";
  }

  public static Event from(@NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                           @NotNull LocalDateTime start,
                           @NotNull LocalDateTime end) {
    return Event.from(UUID.randomUUID(), title, start, end);
  }

  public static Event from(@NotNull UUID id,
                           @NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                           @NotNull LocalDateTime start,
                           @NotNull LocalDateTime end) {
    return Event.from(id, title, start, end, emptyList());
  }

  public static Event from(@NotNull UUID id,
                           @NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                           @NotNull LocalDateTime start,
                           @NotNull LocalDateTime end,
                           @NotNull List<Person> participants) {
    return new Event(id, title, start, end, participants, null, null);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return createdDate == null;
  }

  public Event copy(UnaryOperator<String> titleFunction, UnaryOperator<LocalDateTime> startFunction,
                    UnaryOperator<LocalDateTime> endFunction) {
    return new Event(id, titleFunction.apply(title), startFunction.apply(startTime), endFunction.apply(endTime),
        participants, createdDate, lastModifiedDate);
  }

  public Event copy(UnaryOperator<List<Person>> participantsFunction) {
    return new Event(id, title, startTime, endTime, participantsFunction.apply(participants),
        createdDate, lastModifiedDate);
  }
}
