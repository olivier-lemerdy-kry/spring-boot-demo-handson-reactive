package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class EventUpdateRequestJsonTest {

  @Autowired
  private JacksonTester<EventUpdateRequest> jacksonTester;

  @Test
  void deserialize_empty() throws IOException {
    var eventUpdateRequest = jacksonTester.parseObject("{}");

    assertThat(eventUpdateRequest).isNotNull();
    assertThat(eventUpdateRequest.title()).isEmpty();
    assertThat(eventUpdateRequest.start()).isEmpty();
    assertThat(eventUpdateRequest.end()).isEmpty();
  }

  @Test
  void serialize_empty() throws IOException {
    var eventUpdateRequest = new EventUpdateRequest(Optional.empty(), Optional.empty(), Optional.empty());

    var jsonContent = jacksonTester.write(eventUpdateRequest);

    assertThat(jsonContent).hasEmptyJsonPathValue("$.title").hasEmptyJsonPathValue("$.start").hasEmptyJsonPathValue("$.end");
  }

  @Test
  void deserialize() throws IOException {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT).plusHours(1);
    var end = start.plusHours(12);

    var eventUpdateRequest = jacksonTester.readObject("EventUpdateRequest.json");

    assertThat(eventUpdateRequest).isNotNull();
    assertThat(eventUpdateRequest.title()).hasValue("Some other event");
    assertThat(eventUpdateRequest.start()).hasValue(start);
    assertThat(eventUpdateRequest.end()).hasValue(end);
  }

  @Test
  void serialize() throws IOException {
    var start = LocalDate.of(2001, Month.JANUARY, 1).atTime(LocalTime.MIDNIGHT).plusHours(1);

    var jsonContent = jacksonTester.write(
        new EventUpdateRequest(Optional.of("Some other event"), Optional.of(start), Optional.of(start.plusHours(12))));

    assertThat(jsonContent).isEqualToJson("EventUpdateRequest.json");
  }

}