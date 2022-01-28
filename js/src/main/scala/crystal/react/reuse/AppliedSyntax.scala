package crystal.react.reuse

import japgolly.scalajs.react.Reusability

import scala.reflect.ClassTag

protected trait AppliedSyntax {

  /*
   * Supports construction via the pattern `Reuse(reusedValue).by(valueWithReusability)`
   */
  class Applied[A](valueA: => A) {
    val value: () => A = () => valueA

    def by[R](reuseByR: R)(implicit classTagR: ClassTag[R], reuseR: Reusability[R]): Reuse[A] =
      Reuse.by(reuseByR)(valueA)

    def always: Reuse[A] = Reuse.by(())(valueA)
  }

  implicit class AppliedFn2Ops[A, R, S, B](aa: Applied[A])(implicit ev: A =:= ((R, S) => B)) {
    /*
     * Given a (R, S) => B, instantiate R and build a S ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[S => B] =
      Reuse.by(r)(s => ev(aa.value())(r, s))

    /*
     * Given a (R, S) => B, instantiate R and S and build a Reuse[B].
     */
    def apply(
      r:         R,
      s:         S
    )(implicit
      classTagR: ClassTag[(R, S)],
      reuseR:    Reusability[(R, S)]
    ): Reuse[B] =
      Reuse.by((r, s))(ev(aa.value())(r, s))
  }

  implicit class AppliedFn3Ops[A, R, S, T, B](aa: Applied[A])(implicit ev: A =:= ((R, S, T) => B)) {
    /*
     * Given a (R, S, T) => B , instantiate R and build a (S, T) ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[(S, T) => B] =
      Reuse.by(r)((s, t) => ev(aa.value())(r, s, t))

    /*
     * Given a (R, S, T) => B , instantiate R and S and build a T ==> B.
     */
    def apply(
      r:          R,
      s:          S
    )(implicit
      classTagRS: ClassTag[(R, S)],
      reuseR:     Reusability[(R, S)]
    ): Reuse[T => B] =
      Reuse.by((r, s))(t => ev(aa.value())(r, s, t))

    /*
     * Given a (R, S, T) => B , instantiate R, S and T and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T
    )(implicit
      classTagRS: ClassTag[(R, S, T)],
      reuseR:     Reusability[(R, S, T)]
    ): Reuse[B] =
      Reuse.by((r, s, t))(ev(aa.value())(r, s, t))
  }

  implicit class AppliedFn4Ops[A, R, S, T, U, B](aa: Applied[A])(implicit
    ev:                                              A =:= ((R, S, T, U) => B)
  ) {
    /*
     * Given a (R, S, T, U) => B , instantiate R and build a (S, T, U) ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[(S, T, U) => B] =
      Reuse.by(r)((s, t, u) => ev(aa.value())(r, s, t, u))

    /*
     * Given a (R, S, T, U) => B , instantiate R and S and build a (T, U) ==> B.
     */
    def apply(
      r:          R,
      s:          S
    )(implicit
      classTagRS: ClassTag[(R, S)],
      reuseR:     Reusability[(R, S)]
    ): Reuse[(T, U) => B] =
      Reuse.by((r, s))((t, u) => ev(aa.value())(r, s, t, u))

    /*
     * Given a (R, S, T, U) => B , instantiate R, S and T and build a U ==> B.
     */
    def apply(
      r:          R,
      s:          S,
      t:          T
    )(implicit
      classTagRS: ClassTag[(R, S, T)],
      reuseR:     Reusability[(R, S, T)]
    ): Reuse[U => B] =
      Reuse.by((r, s, t))(u => ev(aa.value())(r, s, t, u))

    /*
     * Given a (R, S, T, U) => B , instantiate R, S, T and U and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U
    )(implicit
      classTagRS: ClassTag[(R, S, T, U)],
      reuseR:     Reusability[(R, S, T, U)]
    ): Reuse[B] =
      Reuse.by((r, s, t, u))(ev(aa.value())(r, s, t, u))
  }

  implicit class AppliedFn5Ops[A, R, S, T, U, V, B](aa: Applied[A])(implicit
    ev:                                                 A =:= ((R, S, T, U, V) => B)
  ) {
    /*
     * Given a (R, S, T, U, V) => B , instantiate R and build a (S, T, U, V) ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[(S, T, U, V) => B] =
      Reuse.by(r)((s, t, u, v) => ev(aa.value())(r, s, t, u, v))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R and S and build a (T, U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S
    )(implicit
      classTagRS: ClassTag[(R, S)],
      reuseR:     Reusability[(R, S)]
    ): Reuse[(T, U, V) => B] =
      Reuse.by((r, s))((t, u, v) => ev(aa.value())(r, s, t, u, v))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S and T and build a (U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S,
      t:          T
    )(implicit
      classTagRS: ClassTag[(R, S, T)],
      reuseR:     Reusability[(R, S, T)]
    ): Reuse[(U, V) => B] =
      Reuse.by((r, s, t))((u, v) => ev(aa.value())(r, s, t, u, v))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T and U and build a V ==> B Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U
    )(implicit
      classTagRS: ClassTag[(R, S, T, U)],
      reuseR:     Reusability[(R, S, T, U)]
    ): Reuse[V => B] =
      Reuse.by((r, s, t, u))(v => ev(aa.value())(r, s, t, u, v))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T, U and V and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V)],
      reuseR:     Reusability[(R, S, T, U, V)]
    ): Reuse[B] =
      Reuse.by((r, s, t, u, v))(ev(aa.value())(r, s, t, u, v))
  }

  implicit class AppliedFn6Ops[A, R, S, T, U, V, W, B](aa: Applied[A])(implicit
    ev:                                                    A =:= ((R, S, T, U, V, W) => B)
  ) {
    /*
     * Given a (R, S, T, U, V) => B , instantiate R and build a (S, T, U, V) ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[(S, T, U, V, W) => B] =
      Reuse.by(r)((s, t, u, v, w) => ev(aa.value())(r, s, t, u, v, w))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R and S and build a (T, U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S
    )(implicit
      classTagRS: ClassTag[(R, S)],
      reuseR:     Reusability[(R, S)]
    ): Reuse[(T, U, V, W) => B] =
      Reuse.by((r, s))((t, u, v, w) => ev(aa.value())(r, s, t, u, v, w))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S and T and build a (U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S,
      t:          T
    )(implicit
      classTagRS: ClassTag[(R, S, T)],
      reuseR:     Reusability[(R, S, T)]
    ): Reuse[(U, V, W) => B] =
      Reuse.by((r, s, t))((u, v, w) => ev(aa.value())(r, s, t, u, v, w))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T and U and build a V ==> B Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U
    )(implicit
      classTagRS: ClassTag[(R, S, T, U)],
      reuseR:     Reusability[(R, S, T, U)]
    ): Reuse[(V, W) => B] =
      Reuse.by((r, s, t, u))((v, w) => ev(aa.value())(r, s, t, u, v, w))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T, U and V and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V)],
      reuseR:     Reusability[(R, S, T, U, V)]
    ): Reuse[W => B] =
      Reuse.by((r, s, t, u, v))(w => ev(aa.value())(r, s, t, u, v, w))

    /*
     * Given a (R, S, T, U, V, W) => B , instantiate R, S, T, U, V and W and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V,
      w:          W
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V, W)],
      reuseR:     Reusability[(R, S, T, U, V, W)]
    ): Reuse[B] =
      Reuse.by((r, s, t, u, v, w))(ev(aa.value())(r, s, t, u, v, w))
  }

  implicit class AppliedFn7Ops[A, R, S, T, U, V, W, X, B](aa: Applied[A])(implicit
    ev:                                                       A =:= ((R, S, T, U, V, W, X) => B)
  ) {
    /*
     * Given a (R, S, T, U, V) => B , instantiate R and build a (S, T, U, V) ==> B.
     */
    def apply(
      r:         R
    )(implicit
      classTagR: ClassTag[R],
      reuseR:    Reusability[R]
    ): Reuse[(S, T, U, V, W, X) => B] =
      Reuse.by(r)((s, t, u, v, w, x) => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R and S and build a (T, U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S
    )(implicit
      classTagRS: ClassTag[(R, S)],
      reuseR:     Reusability[(R, S)]
    ): Reuse[(T, U, V, W, X) => B] =
      Reuse.by((r, s))((t, u, v, w, x) => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S and T and build a (U, V) ==> B.
     */
    def apply(
      r:          R,
      s:          S,
      t:          T
    )(implicit
      classTagRS: ClassTag[(R, S, T)],
      reuseR:     Reusability[(R, S, T)]
    ): Reuse[(U, V, W, X) => B] =
      Reuse.by((r, s, t))((u, v, w, x) => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T and U and build a V ==> B Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U
    )(implicit
      classTagRS: ClassTag[(R, S, T, U)],
      reuseR:     Reusability[(R, S, T, U)]
    ): Reuse[(V, W, X) => B] =
      Reuse.by((r, s, t, u))((v, w, x) => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V) => B , instantiate R, S, T, U and V and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V)],
      reuseR:     Reusability[(R, S, T, U, V)]
    ): Reuse[(W, X) => B] =
      Reuse.by((r, s, t, u, v))((w, x) => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V, W) => B , instantiate R, S, T, U, V and W and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V,
      w:          W
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V, W)],
      reuseR:     Reusability[(R, S, T, U, V, W)]
    ): Reuse[X => B] =
      Reuse.by((r, s, t, u, v, w))(x => ev(aa.value())(r, s, t, u, v, w, x))

    /*
     * Given a (R, S, T, U, V, W) => B , instantiate R, S, T, U, V and W and build a Reuse[B].
     */
    def apply(
      r:          R,
      s:          S,
      t:          T,
      u:          U,
      v:          V,
      w:          W,
      x:          X
    )(implicit
      classTagRS: ClassTag[(R, S, T, U, V, W, X)],
      reuseR:     Reusability[(R, S, T, U, V, W, X)]
    ): Reuse[B] =
      Reuse.by((r, s, t, u, v, w, x))(ev(aa.value())(r, s, t, u, v, w, x))
  }
}
