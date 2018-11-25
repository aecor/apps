package aecor.example.account
import java.util.UUID

import aecor.example.common.Timestamp
import aecor.runtime.Eventsourced
import aecor.runtime.akkapersistence.AkkaPersistenceRuntime
import aecor.util.Clock
import cats.effect.Effect
import cats.implicits._

object deployment {
  def deploy[F[_]: Effect](runtime: AkkaPersistenceRuntime[UUID], clock: Clock[F]): F[Accounts[F]] =
    runtime
      .deploy(
        "Account",
        EventSourcedAlgebra.behavior[F].enrich(clock.instant.map(Timestamp(_))),
        EventSourcedAlgebra.tagging
      )
      .map(Eventsourced.Entities.fromEitherK(_))
}
