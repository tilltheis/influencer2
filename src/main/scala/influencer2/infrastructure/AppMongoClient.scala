package influencer2.infrastructure

import com.mongodb.MongoException
import influencer2.domain.{Post, User}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import influencer2.infrastructure.UserMongoCodec.given_MongoCodecProvider_User
import mongo4cats.zio.{ZClientSession, ZMongoClient, ZMongoCollection, ZMongoDatabase}
import zio.Clock.ClockLive
import zio.{RIO, RLayer, Schedule, Scope, ZIO, ZLayer, durationInt}

import scala.math.Ordered.orderingToOrdered

case class AppMongoClient(
    underlying: ZMongoClient,
    postCollection: ZMongoCollection[Post],
    userCollection: ZMongoCollection[User]
):
  def sessionedScoped[R, A](zio: => RIO[ZClientSession & R, A]): RIO[Scope & R, A] =
    zio.provideSomeLayer(ZLayer(underlying.createSession.orDie))

  def sessioned[R, A](zio: => RIO[ZClientSession & R, A]): RIO[R, A] =
    ZIO.scoped(sessionedScoped(zio))

  def sessionedWith[R, A](f: ZClientSession => RIO[R, A]): RIO[R, A] =
    ZIO.scoped(sessioned(ZIO.serviceWithZIO[ZClientSession](f)))

  def transactedScoped[R, A](zio: => RIO[ZClientSession & R, A]): RIO[Scope & R, A] =
    sessionedScoped {
      val transaction = for
        session <- ZIO.service[ZClientSession]
        _       <- session.startTransaction
        result  <- zio.tapBoth(_ => session.abortTransaction, _ => session.commitTransaction)
      yield result

      val retrySchedule =
        Schedule.recurWhile[Throwable] {
          case e: MongoException if e.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL) => true
          case _                                                                                      => false
        } >>> Schedule.exponential(10.millis) >>> Schedule.elapsed.whileOutput(_ < 100.millis)

      transaction
        .tapErrorCause(ZIO.logWarningCause("transaction failed, maybe retrying", _))
        .retry(retrySchedule)
        .tapErrorCause(ZIO.logWarningCause("transaction failed, giving up", _))
        .withClock(ClockLive)
    }

  def transacted[R, A](zio: => RIO[ZClientSession & R, A]): RIO[R, A] =
    ZIO.scoped(transactedScoped(zio))

  def transactedWith[R, A](f: ZClientSession => RIO[R, A]): RIO[R, A] =
    ZIO.scoped(transacted(ZIO.serviceWithZIO[ZClientSession](f)))
end AppMongoClient

object AppMongoClient:
  val layer: RLayer[ZMongoClient & ZMongoDatabase, AppMongoClient] = ZLayer {
    for
      client         <- ZIO.service[ZMongoClient]
      database       <- ZIO.service[ZMongoDatabase]
      postCollection <- database.getCollectionWithCodec[Post]("posts")
      userCollection <- database.getCollectionWithCodec[User]("users")
    yield AppMongoClient(client, postCollection, userCollection)
  }
