package aecor.example.account

import aecor.data.Enriched
import aecor.example.common.{ Amount, Timestamp }
import aecor.runtime.akkapersistence.serialization.{ PersistentDecoder, PersistentEncoder }
import aecor.util.PersistentEncoderCirce
//
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.Encoder

sealed abstract class AccountEvent extends Product with Serializable

object AccountEvent {
  case class AccountOpened(checkBalance: Boolean) extends AccountEvent

  case class AccountDebited(transactionId: AccountTransactionId, amount: Amount)
      extends AccountEvent

  case class AccountCredited(transactionId: AccountTransactionId, amount: Amount)
      extends AccountEvent

  implicit val encoder: PersistentEncoder[Enriched[Timestamp, AccountEvent]] =
    PersistentEncoderCirce.encoder(Encoder[Enriched[Timestamp, AccountEvent]])

  implicit val decoder: PersistentDecoder[Enriched[Timestamp, AccountEvent]] =
    PersistentEncoderCirce.decoder[Enriched[Timestamp, AccountEvent]]
}
