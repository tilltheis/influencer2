package influencer2

import zio.{Task, ZIO, ZIOAppDefault}

object App extends ZIOAppDefault:
  override def run: Task[Unit] =
    (ZIO.service[HttpModule] *> ZIO.never).unit
      .provide(DatabaseModule.layer, UserModule.layer, HttpModule.layer)
