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
    var uuid = UUID.fromString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");

    var jsonContent = jacksonTester.write(new PersonResponse(uuid, "John Doe"));

    assertThat(jsonContent).isEqualToJson("PersonResponse.json");
  }

  @Test
  void deserialize() throws IOException {
    var person = jacksonTester.readObject("PersonResponse.json");

    assertThat(person).isNotNull();
    assertThat(person.id()).hasToString("38a14a82-d5a2-4210-9d61-cc3577bfa5df");
    assertThat(person.name()).isEqualTo("John Doe");
  }

}