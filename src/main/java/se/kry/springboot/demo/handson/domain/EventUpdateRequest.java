package se.kry.springboot.demo.handson.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.validation.constraints.Size;

public record EventUpdateRequest(
    Optional<@Size(max = EventConstants.Sizes.TITLE) String> title,
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
