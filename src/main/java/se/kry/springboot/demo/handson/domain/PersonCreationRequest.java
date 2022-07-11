package se.kry.springboot.demo.handson.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record PersonCreationRequest(@NotBlank @Size(max = PersonConstants.Sizes.NAME) String name) {
}
