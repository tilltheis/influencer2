package influencer2.infrastructure

import influencer2.domain.{Post, User}
import mongo4cats.zio.{ZClientSession, ZMongoClient, ZMongoCollection, ZMongoDatabase}
import zio.{RLayer, Scope, ZIO, ZLayer}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import influencer2.infrastructure.UserMongoCodec.given_MongoCodecProvider_User

case class AppMongoClient(
    underlying: ZMongoClient,
    postCollection: ZMongoCollection[Post],
    userCollection: ZMongoCollection[User]
):
  def sessionedScoped[R, E, A](zio: => ZIO[ZClientSession & R, E, A]): ZIO[Scope & R, E, A] =
    zio.provideSomeLayer(ZLayer(underlying.createSession.orDie))

  def sessioned[R, E, A](zio: => ZIO[ZClientSession & R, E, A]): ZIO[R, E, A] =
    ZIO.scoped(sessionedScoped(zio))

  def sessionedWith[R, E, A](f: ZClientSession => ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.scoped(sessioned(ZIO.serviceWithZIO[ZClientSession](f)))

  def transactedScoped[R, E, A](zio: => ZIO[ZClientSession & R, E, A]): ZIO[Scope & R, E, A] =
    // Errors and defects are not explicitly handled because the transaction will be aborted automatically when the
    // surrounding session is closed.
    sessioned {
      for
        session <- ZIO.service[ZClientSession]
        _       <- session.startTransaction.orDie
        result  <- zio
        _       <- session.commitTransaction.orDie
      yield result
    }

  def transacted[R, E, A](zio: => ZIO[ZClientSession & R, E, A]): ZIO[R, E, A] =
    ZIO.scoped(transactedScoped(zio))

  def transactedWith[R, E, A](f: ZClientSession => ZIO[R, E, A]): ZIO[R, E, A] =
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
