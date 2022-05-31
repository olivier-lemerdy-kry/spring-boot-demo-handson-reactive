package se.kry.springboot.demo.handson.domain;

import static se.kry.springboot.demo.handson.domain.EventConstants.SIZE_TITLE;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.validation.constraints.Size;

public record EventUpdateRequest(
    Optional<@Size(max = SIZE_TITLE) String> title,
    Optional<LocalDateTime> startTime,
    Optional<LocalDateTime> endTime) {

  public EventUpdateRequest {
    startTime.ifPresent(s -> endTime.ifPresent(e -> {
      if (s.isAfter(e)) {
        throw new StartIsAfterEndException(s, e);
      }
    }));
  }
}
