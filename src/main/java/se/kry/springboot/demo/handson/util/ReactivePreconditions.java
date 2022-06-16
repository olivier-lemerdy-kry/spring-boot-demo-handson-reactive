package se.kry.springboot.demo.handson.util;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public enum ReactivePreconditions {
  ;

  public static <T> Mono<T> requireNonNull(T object) {
    if (object == null) {
      return Mono.error(NullPointerException::new);
    }
    return Mono.just(object);
  }

  public static <T, U> Mono<Tuple2<T, U>> requireNonNull(T object1, U object2) {
    return Mono.zip(
        requireNonNull(object1),
        requireNonNull(object2));
  }
}
