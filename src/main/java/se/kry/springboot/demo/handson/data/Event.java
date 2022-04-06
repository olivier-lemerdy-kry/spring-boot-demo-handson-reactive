package se.kry.springboot.demo.handson.data;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;

public class Event implements Persistable<UUID> {

  @Id
  private UUID id;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return id == null;
  }
}
