package se.kry.springboot.demo.handson.domain;

import java.time.Instant;
import java.util.UUID;

public interface PersonDefaults {

  String ID_STRING = "e1c47fc3-472d-4c14-8d7a-c1b4d1dbdfe5";

  UUID ID = UUID.fromString(ID_STRING);

  String NAME = "John Doe";

  Instant CREATED_DATE = Instant.EPOCH;

  Instant LAST_MODIFIED_DATE = Instant.EPOCH;

  String OTHER_ID_STRING = "b5130fc9-aec2-4a54-b2ae-a709195041cf";

  UUID OTHER_ID = UUID.fromString(OTHER_ID_STRING);

  String OTHER_NAME = "Jane Doe";
}
