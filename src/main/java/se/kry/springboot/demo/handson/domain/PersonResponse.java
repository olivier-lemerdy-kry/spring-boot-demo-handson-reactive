package se.kry.springboot.demo.handson.domain;

import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record PersonResponse(@NotNull UUID id,
                             @NotBlank @Size(max = PersonConstants.Sizes.NAME) String name) {
}
