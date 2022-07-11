package se.kry.springboot.demo.handson.util;

import io.reactivex.rxjava3.core.Maybe;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public enum MaybePreconditions {
  ;

  public static <T> Maybe<T> requireNonNull(T object) {
    if (object == null) {
      return Maybe.error(NullPointerException::new);
    }
    return Maybe.just(object);
  }

  public static <T1, T2> Maybe<Tuple2<T1, T2>> requireNonNull(T1 object1, T2 object2) {
    return Maybe.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        Tuples::of
    );
  }

  public static <T1, T2, T3> Maybe<Tuple3<T1, T2, T3>> requireNonNull(T1 object1, T2 object2, T3 object3) {
    return Maybe.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        requireNonNull(object3),
        Tuples::of
    );
  }
}
