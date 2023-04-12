package influencer2

import zio.http.Server
import zio.{Console, Task, ZIO, ZIOAppDefault}

object App extends ZIOAppDefault:
  override def run: Task[Unit] =
    (ZIO.service[Server] *> Console.readLine("Press ENTER to stop the server\n")).unit
      .provide(DatabaseModule.layer, UserModule.layer, PostModule.layer, HttpModule.layer)
