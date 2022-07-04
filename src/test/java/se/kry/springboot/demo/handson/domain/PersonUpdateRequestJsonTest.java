package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class PersonUpdateRequestJsonTest {

  @Autowired
  private JacksonTester<PersonUpdateRequest> jacksonTester;

  @Test
  void deserialize_empty() throws IOException {
    var eventUpdateRequest = jacksonTester.parseObject("{}");

    assertThat(eventUpdateRequest).isNotNull();
    assertThat(eventUpdateRequest.name()).isEmpty();
  }

  @Test
  void serialize_empty() throws IOException {
    var personUpdateRequest = new PersonUpdateRequest(Optional.empty());

    var jsonContent = jacksonTester.write(personUpdateRequest);

    assertThat(jsonContent).hasEmptyJsonPathValue("$.name");
  }

  @Test
  void deserialize() throws IOException {
    var personUpdateRequest = jacksonTester.readObject("PersonUpdateRequest.json");

    assertThat(personUpdateRequest).isNotNull();
    assertThat(personUpdateRequest.name()).hasValue(PersonDefaults.NAME);
  }

  @Test
  void serialize() throws IOException {
    var personUpdateRequest = new PersonUpdateRequest(Optional.of(PersonDefaults.NAME));
    var jsonContent = jacksonTester.write(personUpdateRequest);

    assertThat(jsonContent).isEqualToJson("PersonUpdateRequest.json");
  }

}