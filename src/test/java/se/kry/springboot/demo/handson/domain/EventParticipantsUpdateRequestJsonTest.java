package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class EventParticipantsUpdateRequestJsonTest {

  @Autowired
  private JacksonTester<EventParticipantsUpdateRequest> jacksonTester;

  @Test
  void serialize() throws IOException {
    var personIds = Stream.of(
        "a04aff4e-c93d-4553-a746-d649b3b2d4bb",
        "35912aed-42b3-4307-946a-8eb1d7d34894",
        "8b983249-59c1-4276-aff0-dab12e240383"
    ).map(UUID::fromString).toList();

    var jsonContent = jacksonTester.write(new EventParticipantsUpdateRequest(personIds));
    assertThat(jsonContent).isEqualToJson("EventParticipantsUpdateRequest.json");
  }

  @Test
  void deserialize() throws IOException {
    var eventParticipants = jacksonTester.readObject("EventParticipantsUpdateRequest.json");

    assertThat(eventParticipants).isNotNull();
    assertThat(eventParticipants.personIds())
        .hasSize(3)
        .containsExactly(
            UUID.fromString("a04aff4e-c93d-4553-a746-d649b3b2d4bb"),
            UUID.fromString("35912aed-42b3-4307-946a-8eb1d7d34894"),
            UUID.fromString("8b983249-59c1-4276-aff0-dab12e240383"));
  }
}
