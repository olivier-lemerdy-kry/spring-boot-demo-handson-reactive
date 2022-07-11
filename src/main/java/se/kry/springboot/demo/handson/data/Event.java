package se.kry.springboot.demo.handson.data;

import static java.util.Collections.emptyList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import se.kry.springboot.demo.handson.domain.EventConstants;

@Document
public record Event(@MongoId UUID id,
                    @NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                    @NotNull LocalDateTime startTime,
                    @NotNull LocalDateTime endTime,
                    @NotNull List<UUID> participantIds,
                    @CreatedDate Instant createdDate,
                    @LastModifiedDate Instant lastModifiedDate) implements Persistable<UUID> {

  public static Event from(@NotBlank @Size(max = EventConstants.Sizes.TITLE) String title, @NotNull LocalDateTime start,
                           @NotNull LocalDateTime end) {
    return new Event(UUID.randomUUID(), title, start, end, emptyList(), null, null);
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
        participantIds, createdDate, lastModifiedDate);
  }

  public Event copy(UnaryOperator<List<UUID>> participantIdsFunction) {
    return new Event(id, title, startTime, endTime, participantIdsFunction.apply(participantIds),
        createdDate, lastModifiedDate);
  }
}
