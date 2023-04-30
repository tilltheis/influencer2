package influencer2.infrastructure

import influencer2.domain.{Post, PostDao, PostId, PostNotFound, UserId}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import mongo4cats.operations.{Filter, Sort, Update}
import mongo4cats.zio.{ZMongoClient, ZMongoCollection, ZMongoDatabase}
import zio.{IO, RLayer, UIO, ZIO, ZLayer}

class MongoPostDao(client: AppMongoClient) extends PostDao:
  override def createPost(post: Post): UIO[Unit] = client.transactedWith { session =>
    client.userCollection.updateOne(session, Filter.idEq(post.userId.value.toString), Update.inc("postCount", 1))
      &> client.postCollection.insertOne(session, post).unit
  }.orDie

  override val loadPosts: UIO[Seq[Post]] = client.sessionedWith { session =>
    client.postCollection.find(session).sort(Sort.desc("createdAt")).all.orDie.map(_.toSeq)
  }

  override def loadPost(postId: PostId): IO[PostNotFound.type, Post] = client.sessionedWith { session =>
    client.postCollection.find(session, Filter.idEq(postId.value.toString)).first.orDie.someOrFail(PostNotFound)
  }

  override def likePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type, Unit] =
    client.sessionedWith { session =>
      client.postCollection
        .updateOne(
          session,
          Filter.idEq(postId.value.toString),
          Update.set(s"likes.${userId.value}", username)
        )
        .orDie
        .flatMap(result => (if (result.getMatchedCount == 1) ZIO.unit else ZIO.fail(PostNotFound)))
    }
  ZIO.unit

object MongoPostDao:
  val layer: RLayer[AppMongoClient, MongoPostDao] = ZLayer.fromFunction(MongoPostDao(_))
