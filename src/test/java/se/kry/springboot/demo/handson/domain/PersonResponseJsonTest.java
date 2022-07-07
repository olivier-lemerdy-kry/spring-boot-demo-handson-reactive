package se.kry.springboot.demo.handson.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
class PersonResponseJsonTest {

  @Autowired
  private JacksonTester<PersonResponse> jacksonTester;

  @Test
  void serialize() throws IOException {
    var personResponse = new PersonResponse(PersonDefaults.ID, PersonDefaults.NAME);
    var jsonContent = jacksonTester.write(personResponse);

    assertThat(jsonContent).isEqualToJson("PersonResponse.json");
  }

  @Test
  void deserialize() throws IOException {
    var personResponse = jacksonTester.readObject("PersonResponse.json");

    assertThat(personResponse).isNotNull();
    assertThat(personResponse.id()).isEqualTo(PersonDefaults.ID);
    assertThat(personResponse.name()).isEqualTo(PersonDefaults.NAME);
  }

}