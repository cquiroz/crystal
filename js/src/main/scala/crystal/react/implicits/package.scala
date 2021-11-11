package crystal.react

import crystal._
import crystal.react.reuse.Reuse
import cats.MonadError
import cats.effect.Sync
import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.component.Generic.MountedSimple
import japgolly.scalajs.react.util.DefaultEffects.{ Async => DefaultA, Sync => DefaultS }
import monocle.Lens
import org.typelevel.log4cats.Logger

import scala.util.control.NonFatal
import japgolly.scalajs.react.util.Effect.UnsafeSync
import japgolly.scalajs.react.util.Effect

package object implicits {
  implicit class DefaultSToOps[A](private val self: DefaultS[A])(implicit
    dispatch:                                       UnsafeSync[DefaultS]
  ) {
    @inline def to[F[_]: Sync]: F[A] = Sync[F].delay(dispatch.runSync(self))

    @inline def toStream[F[_]: Sync]: fs2.Stream[F, A] =
      fs2.Stream.eval(self.to[F])
  }

  implicit class ModMountedSimpleFOps[S, P](
    private val self: MountedSimple[DefaultS, DefaultA, P, S]
  ) extends AnyVal {
    def propsIn[F[_]: Sync]: F[P] = self.props.to[F]
  }

  implicit class StateAccessorFOps[S](
    private val self: StateAccess[DefaultS, DefaultA, S]
  ) extends AnyVal {

    /** Provides access to state `S` in an `F` */
    def stateIn[F[_]: Sync]: F[S] = self.state.to[F]
  }

  implicit class ModStateFOps[S](
    private val self: StateAccess.Write[DefaultS, DefaultA, S]
  ) extends AnyVal {

    def setStateIn[F[_]: Sync](s: S): F[Unit]      = self.setState(s).to[F]
    def modStateIn[F[_]: Sync](f: S => S): F[Unit] = self.modState(f).to[F]

    /** Like `setState` but completes with a `Unit` value *after* the state modification has been
      * completed. In contrast, `setState(mod).to[F]` completes with a unit once the state
      * modification has been enqueued.
      *
      * Provides access only to state.
      */
    def setStateAsyncIn[F[_]: Async](s: S)(implicit dispatch: UnsafeSync[DefaultS]): F[Unit] =
      Async[F].async_[Unit] { cb =>
        val doMod = self.setState(s, DefaultS.delay(cb(Right(()))))
        dispatch.runSync(
          doMod
            .maybeHandleError { case NonFatal(t) =>
              DefaultS.delay(cb(Left(t)))
            }
        )
      }

    /** Like `modState` but completes with a `Unit` value *after* the state modification has been
      * completed. In contrast, `modState(mod).to[F]` completes with a unit once the state
      * modification has been enqueued.
      *
      * Provides access only to state.
      */
    def modStateAsyncIn[F[_]: Async](
      mod:               S => S
    )(implicit dispatch: UnsafeSync[DefaultS]): F[Unit] =
      Async[F].async_[Unit] { asyncCB =>
        val doMod = self.modState(mod, DefaultS.delay(asyncCB(Right(()))))
        dispatch.runSync(
          doMod
            .maybeHandleError { case NonFatal(t) =>
              DefaultS.delay(asyncCB(Left(t)))
            }
        )
      }

    def setStateLIn[F[_]]: SetStateLApplied[F, S] =
      new SetStateLApplied[F, S](self)

    def modStateLIn[F[_]]: ModStateLApplied[F, S] =
      new ModStateLApplied[F, S](self)
  }

  implicit class ModStateWithPropsFOps[S, P](
    private val self: StateAccess.WriteWithProps[DefaultS, DefaultA, P, S]
  ) extends AnyVal {

    /** Like `modState` but completes with a `Unit` value *after* the state modification has been
      * completed. In contrast, `modState(mod).to[F]` completes with a unit once the state
      * modification has been enqueued.
      *
      * Provides access to both state and props.
      */
    def modStateWithPropsIn[F[_]: Async](
      mod:               (S, P) => S
    )(implicit dispatch: UnsafeSync[DefaultS]): F[Unit] =
      Async[F].async_[Unit] { cb =>
        val doMod = self.modState(mod, DefaultS.delay(cb(Right(()))))
        dispatch.runSync(
          doMod
            .maybeHandleError { case NonFatal(t) =>
              DefaultS.delay(cb(Left(t)))
            }
        )
      }
  }

  implicit class EffectAOps[F[_], A](private val self: F[A]) extends AnyVal {

    /** Return a `DefaultS[Unit]` that will run the effect `F[A]` asynchronously.
      *
      * @param cb
      *   Result handler returning a `F[Unit]`.
      */
    def runAsync(
      cb:         Either[Throwable, A] => F[Unit]
    )(implicit F: MonadError[F, Throwable], dispatcher: Effect.Dispatch[F]): DefaultS[Unit] =
      DefaultS.delay(dispatcher.dispatch(self.attempt.flatMap(cb)))

    /** Return a `DefaultS[Unit]` that will run the effect `F[A]` asynchronously.
      *
      * @param cb
      *   Result handler returning a `DefaultS[Unit]`.
      */
    def runAsyncAndThen(
      cb:          Either[Throwable, A] => DefaultS[Unit]
    )(implicit
      F:           Sync[F],
      dispatcherF: Effect.Dispatch[F],
      dispatchS:   UnsafeSync[DefaultS]
    ): DefaultS[Unit] =
      runAsync(cb.andThen(c => F.delay(dispatchS.runSync(c))))

    /** Return a `DefaultS[Unit]` that will run the effect `F[A]` asynchronously and discard the
      * result or errors.
      */
    def runAsyncAndForget(implicit
      F:           MonadError[F, Throwable],
      dispatcherF: Effect.Dispatch[F]
    ): DefaultS[Unit] =
      self.runAsync(_ => F.unit)
  }

  implicit class EffectUnitOps[F[_]](private val self: F[Unit]) extends AnyVal {

    /** Return a `DefaultS[Unit]` that will run the effect `F[Unit]` asynchronously and log possible
      * errors.
      *
      * @param cb
      *   `F[Unit]` to run in case of success.
      */
    def runAsyncAndThenF(
      cb:         F[Unit],
      errorMsg:   String = "Error in F[Unit].runAsyncAndThenF"
    )(implicit
      F:          MonadError[F, Throwable],
      dispatcher: Effect.Dispatch[F],
      logger:     Logger[F]
    ): DefaultS[Unit] =
      new EffectAOps(self).runAsync {
        case Right(()) => cb
        case Left(t)   => logger.error(t)(errorMsg)
      }

    /** Return a `DefaultS[Unit]` that will run the effect `F[Unit]` asynchronously and log possible
      * errors.
      *
      * @param cb
      *   `DefaultS[Unit]` to run in case of success.
      */
    def runAsyncAndThen(
      cb:          DefaultS[Unit],
      errorMsg:    String = "Error in F[Unit].runAsyncAndThen"
    )(implicit
      F:           Sync[F],
      dispatcherF: Effect.Dispatch[F],
      logger:      Logger[F],
      dispatchS:   UnsafeSync[DefaultS]
    ): DefaultS[Unit] =
      runAsyncAndThenF(F.delay(dispatchS.runSync(cb)), errorMsg)

    /** Return a `DefaultS[Unit]` that will run the effect F[Unit] asynchronously and log possible
      * errors.
      */
    def runAsync(
      errorMsg:   String = "Error in F[Unit].runAsync"
    )(implicit
      F:          MonadError[F, Throwable],
      dispatcher: Effect.Dispatch[F],
      logger:     Logger[F]
    ): DefaultS[Unit] =
      runAsyncAndThenF(F.unit, errorMsg)

    def runAsync(implicit
      F:          MonadError[F, Throwable],
      dispatcher: Effect.Dispatch[F],
      logger:     Logger[F]
    ): DefaultS[Unit] =
      runAsync()
  }

  implicit class PotRender[A](val pot: Pot[A]) extends AnyVal {
    def renderPending(f: Long => VdomNode): VdomNode =
      pot match {
        case Pending(start) => f(start)
        case _              => EmptyVdom
      }

    def renderError(f: Throwable => VdomNode): VdomNode =
      pot match {
        case Error(t) => f(t)
        case _        => EmptyVdom
      }

    def renderReady(f: A => VdomNode): VdomNode =
      pot match {
        case Ready(a) => f(a)
        case _        => EmptyVdom
      }
  }

  implicit def throwableReusability: Reusability[Throwable] =
    Reusability.byRef[Throwable]

  implicit def potReusability[A: Reusability](implicit
    throwableReusability: Reusability[Throwable]
  ): Reusability[Pot[A]] =
    Reusability((x, y) =>
      x match {
        case Pending(startx) =>
          y match {
            case Pending(starty) => startx === starty
            case _               => false
          }
        case Error(tx)       =>
          y match {
            case Error(ty) => tx ~=~ ty
            case _         => false
          }
        case Ready(ax)       =>
          y match {
            case Ready(ay) => ax ~=~ ay
            case _         => false
          }
      }
    )

  implicit def viewReusability[F[_], A: Reusability]: Reusability[ViewF[F, A]] =
    Reusability.by(_.get)

  implicit def viewOptReusability[F[_], A: Reusability]: Reusability[ViewOptF[F, A]] =
    Reusability.by(_.get)

  implicit def viewListReusability[F[_], A: Reusability]: Reusability[ViewListF[F, A]] =
    Reusability.by(_.get)

  implicit class ViewFModuleOps(private val viewFModule: ViewF.type) extends AnyVal {
    def fromState: FromStateView = new FromStateView
  }

  implicit class ViewFReuseOps[F[_], G[_], A](private val viewF: ViewOps[F, G, A]) extends AnyVal {
    def reuseSet: Reuse[A => F[Unit]] = Reuse.always(viewF.set)

    def reuseMod: Reuse[(A => A) => F[Unit]] = Reuse.always(viewF.mod)

    def reuseModAndGet(implicit F: Async[F]): Reuse[(A => A) => F[G[A]]] =
      Reuse.always(viewF.modAndGet)
  }
}

package implicits {
  protected class SetStateLApplied[F[_], S](
    private val self: StateAccess.Write[DefaultS, DefaultA, S]
  ) extends AnyVal {
    @inline def apply[A, B](lens: Lens[S, B])(a: A)(implicit conv: A => B, F: Sync[F]): F[Unit] =
      self.modStateIn(lens.replace(conv(a)))
  }

  protected class ModStateLApplied[F[_], S](
    private val self: StateAccess.Write[DefaultS, DefaultA, S]
  ) extends AnyVal {
    @inline def apply[A](lens: Lens[S, A])(f: A => A)(implicit F: Sync[F]): F[Unit] =
      self.modStateIn(lens.modify(f))
  }
}
