package se.kry.springboot.demo.handson.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record EventResponse(@NotNull UUID id,
                            @NotBlank @Size(max = EventConstants.Sizes.TITLE) String title,
                            @NotNull LocalDateTime startTime,
                            @NotNull LocalDateTime endTime) {
}
