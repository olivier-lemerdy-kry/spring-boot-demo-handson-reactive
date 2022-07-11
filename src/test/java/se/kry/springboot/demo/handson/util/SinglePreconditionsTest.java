package se.kry.springboot.demo.handson.util;

import static org.assertj.core.api.Assertions.assertThat;
import static se.kry.springboot.demo.handson.util.SinglePreconditions.requireNonNull;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SinglePreconditionsTest {

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
        .test()
        .assertValue("foobar")
        .assertComplete();
  }

  @Test
  void require_non_null_fails_on_null_value() {
    requireNonNull(null)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void require_non_null_succeeds_on_non_null_tuple2() {
    requireNonNull("foo", "bar")
        .test()
        .assertValue(tuple -> {
          assertThat(tuple.getT1()).isEqualTo("foo");
          assertThat(tuple.getT2()).isEqualTo("bar");
          return true;
        }).assertComplete();
  }

  @ParameterizedTest
  @MethodSource("require_non_null_invalid_tuple2_arguments")
  void require_non_null_fails_on_invalid_tuple2(String object1, String object2) {
    requireNonNull(object1, object2)
        .test()
        .assertError(NullPointerException.class);
  }

  @Test
  void require_non_null_succeeds_on_non_null_tuple3() {
    requireNonNull("foo", "bar", "baz")
        .test()
        .assertValue(tuple -> {
          assertThat(tuple.getT1()).isEqualTo("foo");
          assertThat(tuple.getT2()).isEqualTo("bar");
          assertThat(tuple.getT3()).isEqualTo("baz");
          return true;
        }).assertComplete();
  }

  @ParameterizedTest
  @MethodSource("require_non_null_invalid_tuple3_arguments")
  void require_non_null_fails_on_invalid_tuple3(String object1, String object2, String object3) {
    requireNonNull(object1, object2, object3)
        .test()
        .assertError(NullPointerException.class);
  }

}