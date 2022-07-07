package se.kry.springboot.demo.handson.domain;

import java.util.Optional;
import javax.validation.constraints.Size;

public record PersonUpdateRequest(Optional<@Size(max = PersonConstants.Sizes.NAME) String> name) {
}
