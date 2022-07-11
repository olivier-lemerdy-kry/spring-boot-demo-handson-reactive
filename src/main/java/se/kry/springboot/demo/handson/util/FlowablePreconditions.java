package se.kry.springboot.demo.handson.util;

import io.reactivex.Flowable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public enum FlowablePreconditions {
  ;

  public static <T> Flowable<T> requireNonNull(T object) {
    if (object == null) {
      return Flowable.error(NullPointerException::new);
    }
    return Flowable.just(object);
  }

  public static <T1, T2> Flowable<Tuple2<T1, T2>> requireNonNull(T1 object1, T2 object2) {
    return Flowable.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        Tuples::of
    );
  }

  public static <T1, T2, T3> Flowable<Tuple3<T1, T2, T3>> requireNonNull(T1 object1, T2 object2, T3 object3) {
    return Flowable.zip(
        requireNonNull(object1),
        requireNonNull(object2),
        requireNonNull(object3),
        Tuples::of
    );
  }
}
