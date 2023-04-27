package influencer2.infrastructure

import influencer2.domain.{Post, PostDao}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import mongo4cats.operations.{Filter, Sort, Update}
import mongo4cats.zio.{ZMongoClient, ZMongoCollection, ZMongoDatabase, startSessionFixed}
import zio.{RLayer, UIO, ZIO, ZLayer}

class MongoPostDao(client: AppMongoClient) extends PostDao:
  def createPost(post: Post): UIO[Unit] = client.transactedWith { session =>
    client.userCollection.updateOne(session, Filter.eq("_id", post.userId.value.toString), Update.inc("postCount", 1))
      &> client.postCollection.insertOne(session, post).unit
  }.orDie

  val loadPosts: UIO[Seq[Post]] = client.sessionedWith { session =>
    client.postCollection.find(session).sort(Sort.desc("createdAt")).all.orDie.map(_.toSeq)
  } 

object MongoPostDao:
  val layer: RLayer[AppMongoClient, MongoPostDao] = ZLayer.fromFunction(MongoPostDao(_))
