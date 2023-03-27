package influencer2

import mongo4cats.bson.{BsonValue, Document}
import mongo4cats.zio.{ZMongoClient, ZMongoDatabase}
import zio.{TaskLayer, ZIO, ZLayer}

object TestDatabase:
  // creates a randomly named database and drops it when it goes out of scope
  def layer: TaskLayer[ZMongoDatabase] =
    // all the values below should come from a config/secret store/...
    ZLayer.scoped {
      val params = "?connectTimeoutMS=1000&socketTimeoutMS=1000&serverSelectionTimeoutMS=1000"
      for
        client   <- ZMongoClient.fromConnectionString(s"mongodb://root:example@localhost:27017/$params")
        uuid     <- zio.Random.RandomLive.nextUUID
        database <- client.getDatabase(s"influencer2-test_${uuid.toString}")
        // use low level api because for some reason `database.drop.orDie` has no effect
        _ <- ZIO.addFinalizer(database.runCommand(Document("dropDatabase" -> BsonValue.int(1))).orDie)
      yield database
    }
