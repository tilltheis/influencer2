package influencer2.infrastructure

import mongo4cats.models.client.ClientSessionOptions
import mongo4cats.zio.{ZClientSession, ZMongoClient}
import zio.{RIO, Scope, ZIO}

extension (client: ZMongoClient)
  def createSession(options: ClientSessionOptions): RIO[Scope, ZClientSession] =
    ZIO.acquireRelease(client.startSession(options))(x => ZIO.succeed(x.underlying.close()))
  def createSession: RIO[Scope, ZClientSession] = createSession(ClientSessionOptions.apply())
