package se.kry.springboot.demo.handson.util;

import io.reactivex.Single;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public enum ReactivePreconditions {
  ;

  public static <T> Single<T> requireNonNull(T object) {
    if (object == null) {
      return Single.error(NullPointerException::new);
    }
    return Single.just(object);
  }

  public static <T1, T2> Single<Tuple2<T1, T2>> requireNonNull(T1 object1, T2 object2) {
    return Single.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        Tuples::of
    );
  }

  public static <T1, T2, T3> Single<Tuple3<T1, T2, T3>> requireNonNull(T1 object1, T2 object2, T3 object3) {
    return Single.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        requireNonNull(object3),
        Tuples::of
    );
  }
}
