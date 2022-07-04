package se.kry.springboot.demo.handson.domain;

import java.time.Instant;
import java.util.UUID;

public interface PersonDefaults {

  String ID_STRING = "e1c47fc3-472d-4c14-8d7a-c1b4d1dbdfe5";

  UUID ID = UUID.fromString(ID_STRING);

  String NAME = "John Doe";

  String OTHER_NAME = "Jane Doe";

  Instant CREATED_DATE = Instant.EPOCH;

  Instant LAST_MODIFIED_DATE = Instant.EPOCH;
}
