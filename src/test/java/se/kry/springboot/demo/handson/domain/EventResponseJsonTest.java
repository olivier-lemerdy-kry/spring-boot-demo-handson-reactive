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
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT);
    var end = start.plusHours(12);

    var jsonContent = jacksonTester.write(new EventResponse(uuid, "Some event", start, end));

    assertThat(jsonContent).isEqualToJson("EventResponse.json");
  }

  @Test
  void deserialize() throws IOException {
    var event = jacksonTester.readObject("EventResponse.json");

    assertThat(event).isNotNull();
    assertThat(event.id()).hasToString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    assertThat(event.title()).isEqualTo("Some event");
    assertThat(event.startTime()).hasToString("2001-01-01T00:00");
    assertThat(event.endTime()).hasToString("2001-01-01T12:00");
  }

}