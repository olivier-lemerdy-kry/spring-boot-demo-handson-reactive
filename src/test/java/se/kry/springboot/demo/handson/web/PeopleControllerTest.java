package se.kry.springboot.demo.handson.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(PeopleController.class)
class PeopleControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void create_people() {

  }
}
