package se.kry.springboot.demo.handson.data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.UnaryOperator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import se.kry.springboot.demo.handson.domain.EventConstants;

@Table
public record Event(@Id UUID id,
                    @NotBlank @Size(max = EventConstants.SIZE_TITLE) String title,
                    @NotNull LocalDateTime start,
                    @NotNull LocalDateTime end,
                    @CreatedDate Instant createdDate,
                    @LastModifiedDate Instant lastModifiedDate) implements Persistable<UUID> {

  public static Event from(@NotBlank @Size(max = EventConstants.SIZE_TITLE) String title, @NotNull LocalDateTime start,
                           @NotNull LocalDateTime end) {
    return new Event(UUID.randomUUID(), title, start, end, null, null);
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
    return new Event(id, titleFunction.apply(title), startFunction.apply(start), endFunction.apply(end), createdDate,
        lastModifiedDate);
  }
}
