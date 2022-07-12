package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
    assertThat(eventUpdateRequest.startTime()).isEmpty();
    assertThat(eventUpdateRequest.endTime()).isEmpty();
  }

  @Test
  void serialize_empty() throws IOException {
    var eventUpdateRequest = new EventUpdateRequest(Optional.empty(), Optional.empty(), Optional.empty());

    var jsonContent = jacksonTester.write(eventUpdateRequest);

    assertThat(jsonContent)
        .hasEmptyJsonPathValue("$.title")
        .hasEmptyJsonPathValue("$.startTime")
        .hasEmptyJsonPathValue("$.endTime");
  }

  @Test
  void deserialize() throws IOException {
    var eventUpdateRequest = jacksonTester.readObject("EventUpdateRequest.json");

    assertThat(eventUpdateRequest).isNotNull();
    assertThat(eventUpdateRequest.title()).hasValue(EventDefaults.OTHER_TITLE);
    assertThat(eventUpdateRequest.startTime()).hasValue(EventDefaults.OTHER_START_TIME);
    assertThat(eventUpdateRequest.endTime()).hasValue(EventDefaults.OTHER_END_TIME);
  }

  @Test
  void serialize() throws IOException {
    var jsonContent = jacksonTester.write(
        new EventUpdateRequest(
            Optional.of(EventDefaults.OTHER_TITLE),
            Optional.of(EventDefaults.OTHER_START_TIME),
            Optional.of(EventDefaults.OTHER_END_TIME)));

    assertThat(jsonContent).isEqualToJson("EventUpdateRequest.json");
  }

}