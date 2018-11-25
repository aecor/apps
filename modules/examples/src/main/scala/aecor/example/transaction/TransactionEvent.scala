package aecor.example.transaction

import aecor.data.Enriched
import aecor.es.DomainEvent
import aecor.example.account.AccountId
import aecor.example.common.{ Amount, Timestamp }
import aecor.runtime.akkapersistence.serialization.{ PersistentDecoder, PersistentEncoder }
import aecor.util.persistentEncoderUtil
import io.circe.generic.auto._
import io.circe.java8.time._

sealed abstract class TransactionEvent extends DomainEvent

object TransactionEvent {
  final case class TransactionCreated(fromAccount: From[AccountId],
                                      toAccount: To[AccountId],
                                      amount: Amount)
      extends TransactionEvent

  final case object TransactionAuthorized extends TransactionEvent

  case class TransactionFailed(reason: String) extends TransactionEvent

  case object TransactionSucceeded extends TransactionEvent

  implicit val encoder: PersistentEncoder[Enriched[Timestamp, TransactionEvent]] =
    persistentEncoderUtil.circePersistentEncoder
  implicit val decoder: PersistentDecoder[Enriched[Timestamp, TransactionEvent]] =
    persistentEncoderUtil.circePersistentDecoder
}
