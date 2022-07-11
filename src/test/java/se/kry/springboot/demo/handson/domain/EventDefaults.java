package se.kry.springboot.demo.handson.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public interface EventDefaults {

  String ID_STRING = "38a14a82-d5a2-4210-9d61-cc3577bfa5df";

  UUID ID = UUID.fromString(ID_STRING);

  String TITLE = "Some event";

  String START_TIME_STRING = "2001-01-01T12:00:00";

  LocalDateTime START_TIME = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.NOON);

  String END_TIME_STRING = "2001-01-01T13:00:00";

  LocalDateTime END_TIME = START_TIME.plusHours(1);

  List<UUID> PARTICIPANT_IDS = Collections.emptyList();

  Instant CREATED_DATE = Instant.EPOCH;

  Instant LAST_MODIFIED_DATE = Instant.EPOCH;

  String OTHER_ID_STRING = "8ebea9a7-e0ef-4a62-a729-aff26134f9d8";

  UUID OTHER_ID = UUID.fromString(OTHER_ID_STRING);

  String OTHER_TITLE = "Another event";

  String OTHER_START_TIME_STRING = END_TIME_STRING;

  LocalDateTime OTHER_START_TIME = END_TIME;

  String OTHER_END_TIME_STRING = "2001-01-01T14:00:00";

  LocalDateTime OTHER_END_TIME = OTHER_START_TIME.plusHours(1);
}
