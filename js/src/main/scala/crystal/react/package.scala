package crystal

import cats.effect.Async
import crystal.react.reuse.Reuse
import japgolly.scalajs.react._
import japgolly.scalajs.react.util.DefaultEffects.{Async => DefaultA}
import japgolly.scalajs.react.util.DefaultEffects.{Sync => DefaultS}
import japgolly.scalajs.react.util.Effect
import japgolly.scalajs.react.util.Effect.UnsafeSync
import japgolly.scalajs.react.vdom.VdomNode
import org.typelevel.log4cats.Logger

package object react  {
  type SetState[F[_], A] = A => F[Unit]
  type ModState[F[_], A] = (A => A) => F[Unit]

  type ContextComponent[S, B] =
    ScalaComponent[
      Reuse[VdomNode],
      S,
      B,
      CtorType.Props
    ]

  type StateComponent[S, B] =
    ScalaComponent[
      Reuse[ViewF[DefaultS, S] => VdomNode],
      S,
      B,
      CtorType.Props
    ]

  implicit class StreamOps[F[_], A](private val s: fs2.Stream[F, A]) {
    def render(implicit
      async:      Async[F],
      dispatcher: Effect.Dispatch[F],
      logger:     Logger[F],
      reuse:      Reusability[A]
    ): StreamRenderer.Component[A] =
      StreamRenderer.build(s)
  }
}

package react {
  class FromStateView {
    def apply[S]($ : StateAccess[DefaultS, DefaultA, S])(implicit
      dispatch:      UnsafeSync[DefaultS]
    ): ViewF[DefaultS, S] =
      ViewF[DefaultS, S](
        dispatch.runSync($.state),
        (f, cb) => $.modState(f, $.state.flatMap(cb))
      )
  }
}
