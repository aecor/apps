package aecor.example.transaction

import aecor.MonadActionReject
import aecor.data.Folded.syntax._
import aecor.data._
import aecor.example.account.AccountId
import aecor.example.common.Amount
import aecor.example.transaction.Algebra.TransactionInfo
import aecor.example.transaction.EventsourcedAlgebra.TransactionState
import aecor.example.transaction.EventsourcedAlgebra.TransactionStatus.{
  Authorized,
  Failed,
  Requested,
  Succeeded
}
import aecor.example.transaction.TransactionEvent._
import cats.Monad
import cats.implicits._

class EventsourcedAlgebra[F[_]](
  implicit F: MonadActionReject[F, Option[TransactionState], TransactionEvent, String]
) extends Algebra[F] {
  import F._
  override def create(fromAccountId: From[AccountId],
                      toAccountId: To[AccountId],
                      amount: Amount): F[Unit] =
    read.flatMap {
      case None =>
        append(TransactionEvent.TransactionCreated(fromAccountId, toAccountId, amount))
      case Some(_) =>
        ().pure[F]
    }

  override def authorize: F[Unit] =
    read.flatMap {
      case Some(transaction) =>
        if (transaction.status == Requested) {
          append(TransactionAuthorized)
        } else if (transaction.status == Authorized) {
          ().pure[F]
        } else {
          reject("Illegal transition")
        }
      case None =>
        reject("Transaction not found")
    }

  override def fail(reason: String): F[Unit] =
    read.flatMap {
      case Some(transaction) =>
        if (transaction.status == Failed) {
          ().pure[F]
        } else {
          append(TransactionFailed(reason))
        }
      case None =>
        reject("Transaction not found")
    }

  override def succeed: F[Unit] =
    read.flatMap {
      case Some(transaction) =>
        if (transaction.status == Succeeded) {
          ().pure[F]
        } else if (transaction.status == Authorized) {
          append(TransactionSucceeded)
        } else {
          reject("Illegal transition")
        }
      case None =>
        reject("Transaction not found")
    }

  override def getInfo: F[TransactionInfo] =
    read.flatMap {
      case Some(TransactionState(status, from, to, amount)) =>
        TransactionInfo(from, to, amount, Some(status).collect {
          case Succeeded => true
          case Failed    => false
        }).pure[F]
      case None =>
        reject("Transaction not found")
    }
}

object EventsourcedAlgebra {
  def apply[F[_]: MonadActionReject[?[_], Option[TransactionState], TransactionEvent, String]]
    : Algebra[F] =
    new EventsourcedAlgebra

  def behavior[F[_]: Monad]: EventsourcedBehavior[EitherK[Algebra, String, ?[_]], F, Option[TransactionState], TransactionEvent] =
    EventsourcedBehavior
      .optionalRejectable[Algebra, F, TransactionState, TransactionEvent, String](
        apply,
        TransactionState.fromEvent,
        _.applyEvent(_)
      )

  def tagging: Tagging[TransactionId] =
    Tagging.partitioned(20)(EventTag("Transaction"))

  sealed abstract class TransactionStatus

  object TransactionStatus {
    case object Requested extends TransactionStatus
    case object Authorized extends TransactionStatus
    case object Failed extends TransactionStatus
    case object Succeeded extends TransactionStatus
  }

  final case class TransactionState(status: TransactionStatus,
                                    from: From[AccountId],
                                    to: To[AccountId],
                                    amount: Amount)
      extends Product with Serializable {

    def applyEvent(event: TransactionEvent): Folded[TransactionState] = event match {
      case TransactionCreated(_, _, _) => impossible
      case TransactionAuthorized       => copy(status = TransactionStatus.Authorized).next
      case TransactionFailed(_)        => copy(status = TransactionStatus.Failed).next
      case TransactionSucceeded        => copy(status = TransactionStatus.Succeeded).next
    }
  }

  object TransactionState {
    def fromEvent(event: TransactionEvent): Folded[TransactionState] = event match {

      case TransactionEvent.TransactionCreated(fromAccount, toAccount, amount) =>
        TransactionState(TransactionStatus.Requested, fromAccount, toAccount, amount).next

      case _ => impossible
    }
  }

}
