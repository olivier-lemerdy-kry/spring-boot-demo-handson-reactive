package se.kry.springboot.demo.handson.util;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

public enum FluxPreconditions {
  ;

  public static <T> Flux<T> requireNonNull(T object) {
    if (object == null) {
      return Flux.error(NullPointerException::new);
    }
    return Flux.just(object);
  }

  public static <T1, T2> Flux<Tuple2<T1, T2>> requireNonNull(T1 object1, T2 object2) {
    return Flux.zip(
        requireNonNull(object1),
        requireNonNull(object2)
    );
  }

  public static <T1, T2, T3> Flux<Tuple3<T1, T2, T3>> requireNonNull(T1 object1, T2 object2, T3 object3) {
    return Flux.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        requireNonNull(object3)
    );
  }
}
