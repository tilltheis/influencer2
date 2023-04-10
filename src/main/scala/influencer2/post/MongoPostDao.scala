package influencer2.post

import influencer2.post.PostMongoCodec.given_MongoCodecProvider_Post
import mongo4cats.zio.{ZMongoCollection, ZMongoDatabase}
import zio.{RLayer, ZIO, ZLayer}

class MongoPostDao(collection: ZMongoCollection[Post]) extends PostDao

object MongoPostDao:
  val layer: RLayer[ZMongoDatabase, MongoPostDao] = ZLayer {
    for
      database   <- ZIO.service[ZMongoDatabase]
      collection <- database.getCollectionWithCodec[Post]("posts")
    yield MongoPostDao(collection)
  }
