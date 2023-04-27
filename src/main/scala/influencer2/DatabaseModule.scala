package influencer2

import influencer2.infrastructure.AppMongoClient
import mongo4cats.bson.{BsonValue, Document}
import mongo4cats.zio.{ZMongoClient, ZMongoDatabase}
import zio.{TaskLayer, ZEnvironment, ZLayer}

object DatabaseModule:
  private val mongo4catsLayer: TaskLayer[ZMongoClient & ZMongoDatabase] =
    // all the values below should come from a config/secret store/...
    ZLayer.scopedEnvironment {
      val params = "?connectTimeoutMS=1000&socketTimeoutMS=1000&serverSelectionTimeoutMS=1000"
      for
        client   <- ZMongoClient.fromConnectionString(s"mongodb://root:example@localhost:27017/$params")
        database <- client.getDatabase("influencer2")
        _        <- database.runCommand(Document("ping" -> BsonValue.int(1)))
      yield ZEnvironment(client, database)
    }

  val layer: TaskLayer[AppMongoClient] = ZLayer.make[AppMongoClient](AppMongoClient.layer, mongo4catsLayer)
