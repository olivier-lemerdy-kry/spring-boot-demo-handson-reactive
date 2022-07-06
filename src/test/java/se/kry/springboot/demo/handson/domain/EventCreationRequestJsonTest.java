package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class EventCreationRequestJsonTest {

  @Autowired
  private JacksonTester<EventCreationRequest> jacksonTester;

  @Test
  void serialize() throws IOException {
    var jsonContent = jacksonTester.write(
        new EventCreationRequest(EventDefaults.TITLE, EventDefaults.START_TIME, EventDefaults.END_TIME));

    assertThat(jsonContent).isEqualToJson("EventCreationRequest.json");
  }

  @Test
  void deserialize() throws IOException {
    var event = jacksonTester.readObject("EventCreationRequest.json");

    assertThat(event).isNotNull();
    assertThat(event.title()).isEqualTo(EventDefaults.TITLE);
    assertThat(event.startTime()).isEqualTo(EventDefaults.START_TIME);
    assertThat(event.endTime()).isEqualTo(EventDefaults.END_TIME);
  }

}