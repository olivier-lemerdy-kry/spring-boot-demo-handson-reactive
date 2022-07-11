package se.kry.springboot.demo.handson.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.kry.springboot.demo.handson.util.FlowablePreconditions.requireNonNull;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

class FlowablePreconditionsTest {

  static Stream<String[]> require_non_null_invalid_tuple2_arguments() {
    return Stream.of(
        new String[] {null, null},
        new String[] {"foo", null},
        new String[] {null, "bar"}
    );
  }

  static Stream<String[]> require_non_null_invalid_tuple3_arguments() {
    return Stream.of(
        new String[] {null, null, null},
        new String[] {"foo", null, null},
        new String[] {null, "bar", null},
        new String[] {null, null, "baz"},
        new String[] {"foo", "bar", null},
        new String[] {"foo", null, "baz"},
        new String[] {null, "bar", "baz"}
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
  void require_non_null_succeeds_on_non_null_tuple2() {
    requireNonNull("foo", "bar")
        .as(StepVerifier::create)
        .assertNext(tuple -> {
          assertThat(tuple.getT1()).isEqualTo("foo");
          assertThat(tuple.getT2()).isEqualTo("bar");
        }).verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("require_non_null_invalid_tuple2_arguments")
  void require_non_null_fails_on_invalid_tuple2(String object1, String object2) {
    requireNonNull(object1, object2)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

  @Test
  void require_non_null_succeeds_on_non_null_tuple3() {
    requireNonNull("foo", "bar", "baz")
        .as(StepVerifier::create)
        .assertNext(tuple -> {
          assertThat(tuple.getT1()).isEqualTo("foo");
          assertThat(tuple.getT2()).isEqualTo("bar");
          assertThat(tuple.getT3()).isEqualTo("baz");
        }).verifyComplete();
  }

  @ParameterizedTest
  @MethodSource("require_non_null_invalid_tuple3_arguments")
  void require_non_null_fails_on_invalid_tuple3(String object1, String object2, String object3) {
    requireNonNull(object1, object2, object3)
        .as(StepVerifier::create)
        .verifyError(NullPointerException.class);
  }

}