package influencer2

import zio.http.Server
import zio.{Task, ZIO, ZIOAppDefault}

object App extends ZIOAppDefault:
  override def run: Task[Unit] =
    (ZIO.service[Server] *> ZIO.never).unit
      .provide(DatabaseModule.layer, UserModule.layer, PostModule.layer, HttpModule.layer)
