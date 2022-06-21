package se.kry.springboot.demo.handson.data;

import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import se.kry.springboot.demo.handson.domain.PersonConstants;

@Table
public record Person(@Id UUID id,
                     @NotBlank @Size(max = PersonConstants.Sizes.NAME) String name,
                     @CreatedDate Instant createdDate,
                     @LastModifiedDate Instant lastModifiedDate) implements Persistable<UUID> {

  public static Person from(@NotBlank @Size(max = PersonConstants.Sizes.NAME) String name) {
    return new Person(UUID.randomUUID(), name, null, null);
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
