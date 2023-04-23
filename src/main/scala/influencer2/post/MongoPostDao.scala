package influencer2.post

import influencer2.post.PostMongoCodec.given_MongoCodecProvider_Post
import mongo4cats.operations.Sort
import mongo4cats.zio.{ZMongoCollection, ZMongoDatabase}
import zio.{RLayer, UIO, ZIO, ZLayer}

class MongoPostDao(collection: ZMongoCollection[Post]) extends PostDao:
  def createPost(post: Post): UIO[Unit] = collection.insertOne(post).orDie.unit
  val loadPosts: UIO[Seq[Post]]         = collection.find.sort(Sort.desc("createdAt")).all.orDie.map(_.toSeq)

object MongoPostDao:
  val layer: RLayer[ZMongoDatabase, MongoPostDao] = ZLayer {
    for
      database   <- ZIO.service[ZMongoDatabase]
      collection <- database.getCollectionWithCodec[Post]("posts")
    yield MongoPostDao(collection)
  }
