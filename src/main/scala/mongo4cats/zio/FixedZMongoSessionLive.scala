package mongo4cats.zio

import com.mongodb.reactivestreams.client.ClientSession
import mongo4cats.models.client.{ClientSessionOptions, TransactionOptions}
import mongo4cats.zio.syntax.{MongoEmptyStreamException, PublisherSyntax, TaskOptionSyntax}
import zio.{RIO, Scope, Task, ZIO}

case class FixedZClientSessionLive(override val underlying: ClientSession) extends ZClientSession:
  override def startTransaction(options: TransactionOptions): Task[Unit] =
    ZIO.attempt(underlying.startTransaction(options))
  override def abortTransaction: Task[Unit]  = underlying.abortTransaction().asyncVoid
  override def commitTransaction: Task[Unit] = underlying.commitTransaction().asyncVoid

  def close: Task[Unit] = ZIO.attempt(underlying.close())

extension (client: ZMongoClient)
  def startSessionFixed(options: ClientSessionOptions): Task[ZClientSession] =
    client.underlying.startSession(options).asyncSingle.unNone.map(FixedZClientSessionLive.apply)
  def startSessionFixed: Task[ZClientSession] = startSessionFixed(ClientSessionOptions.apply())

  def createSession(options: ClientSessionOptions): RIO[Scope, ZClientSession] =
    ZIO.fromAutoCloseable(client.underlying.startSession(options).asyncSingle.unNone).map(FixedZClientSessionLive.apply)
  def createSession: RIO[Scope, ZClientSession] = createSession(ClientSessionOptions.apply())
