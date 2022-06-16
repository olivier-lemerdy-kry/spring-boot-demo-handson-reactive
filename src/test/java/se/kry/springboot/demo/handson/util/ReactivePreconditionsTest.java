package se.kry.springboot.demo.handson.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.kry.springboot.demo.handson.util.ReactivePreconditions.requireNonNull;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

class ReactivePreconditionsTest {

  static Stream<Arguments> require_non_null_invalid_tuple_arguments() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("foo", null),
        Arguments.of(null, "bar")
    );
  }

  @Test
  void require_non_null_succeeds_on_non_null_value() {
    requireNonNull("foobar")
        .as(StepVerifier::create)
        .assertNext(value -> assertThat(value).isEqualTo("foobar"))
        .verifyComplete();
  }

  @Test
  void require_non_null_fails_on_null_value() {
    requireNonNull(null)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void require_non_null_succeeds_on_non_null_tuple() {
    requireNonNull("foo", "bar")
        .as(StepVerifier::create)
        .assertNext(tuple -> {
          assertThat(tuple.getT1()).isEqualTo("foo");
          assertThat(tuple.getT2()).isEqualTo("bar");
        }).verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("require_non_null_invalid_tuple_arguments")
  void require_non_null_fails_on_invalid_tuple(String object1, String object2) {
    requireNonNull(object1, object2)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

}