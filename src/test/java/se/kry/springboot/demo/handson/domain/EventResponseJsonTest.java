package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class EventResponseJsonTest {

  @Autowired
  private JacksonTester<EventResponse> jacksonTester;

  @Test
  void serialize() throws IOException {
    var jsonContent = jacksonTester.write(
        new EventResponse(EventDefaults.ID, EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME));

    assertThat(jsonContent).isEqualToJson("EventResponse.json");
  }

  @Test
  void deserialize() throws IOException {
    var event = jacksonTester.readObject("EventResponse.json");

    assertThat(event).isNotNull();
    assertThat(event.id()).isEqualTo(EventDefaults.ID);
    assertThat(event.title()).isEqualTo(EventDefaults.TITLE);
    assertThat(event.startTime()).isEqualTo(EventDefaults.START_TIME);
    assertThat(event.endTime()).isEqualTo(EventDefaults.END_TIME);
  }

}