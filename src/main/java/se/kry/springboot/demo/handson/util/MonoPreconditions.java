package se.kry.springboot.demo.handson.util;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

public enum MonoPreconditions {
  ;

  public static <T> Mono<T> requireNonNull(T object) {
    if (object == null) {
      return Mono.error(NullPointerException::new);
    }
    return Mono.just(object);
  }

  public static <T1, T2> Mono<Tuple2<T1, T2>> requireNonNull(T1 object1, T2 object2) {
    return Mono.zip(
        requireNonNull(object1),
        requireNonNull(object2)
    );
  }

  public static <T1, T2, T3> Mono<Tuple3<T1, T2, T3>> requireNonNull(T1 object1, T2 object2, T3 object3) {
    return Mono.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        requireNonNull(object3)
    );
  }
}
