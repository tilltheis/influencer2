package influencer2.infrastructure

import influencer2.domain.{Post, PostDao}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import mongo4cats.operations.{Filter, Sort, Update}
import mongo4cats.zio.{ZMongoClient, ZMongoCollection, ZMongoDatabase, startSessionFixed}
import zio.{RLayer, UIO, ZIO, ZLayer}

class MongoPostDao(client: ZMongoClient, postCollection: ZMongoCollection[Post], userCollection: ZMongoCollection[?])
    extends PostDao:
  def createPost(post: Post): UIO[Unit] =
    (for
      session <- client.startSessionFixed
      _       <- session.startTransaction
      _ <- userCollection.updateOne(session, Filter.eq("_id", post.userId.value.toString), Update.inc("postCount", 1))
      _ <- postCollection.insertOne(session, post).unit
      _ <- session.commitTransaction
    yield ()).orDie

  val loadPosts: UIO[Seq[Post]] = postCollection.find.sort(Sort.desc("createdAt")).all.orDie.map(_.toSeq)

object MongoPostDao:
  val layer: RLayer[ZMongoClient & ZMongoDatabase, MongoPostDao] = ZLayer {
    for
      client         <- ZIO.service[ZMongoClient]
      database       <- ZIO.service[ZMongoDatabase]
      postCollection <- database.getCollectionWithCodec[Post]("posts")
      userCollection <- database.getCollection("users")
    yield MongoPostDao(client, postCollection, userCollection)
  }
