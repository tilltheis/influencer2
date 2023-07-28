package influencer2.infrastructure

import influencer2.domain.{Post, PostAlreadyLiked, PostDao, PostId, PostNotFound, PostNotLiked, UserId}
import influencer2.infrastructure.PostMongoCodec.given_MongoCodecProvider_Post
import influencer2.infrastructure.TransactionDecision.Commit
import mongo4cats.operations.{Filter, Sort, Update}
import mongo4cats.zio.{ZMongoClient, ZMongoCollection, ZMongoDatabase}
import zio.{IO, RLayer, UIO, ZIO, ZLayer}

class MongoPostDao(client: AppMongoClient) extends PostDao:
  override def createPost(post: Post): UIO[Unit] = client.transactedWith { session =>
    client.userCollection.updateOne(session, Filter.idEq(post.userId.value.toString), Update.inc("postCount", 1))
      &> client.postCollection.insertOne(session, post) *> ZIO.succeed(Commit(()))
  }.orDie

  override val loadAllPosts: UIO[Seq[Post]] = client.sessionedWith { session =>
    client.postCollection.find(session).sort(Sort.desc("createdAt")).all.map(_.toSeq)
  }.orDie

  override def loadUserPosts(username: String): UIO[Seq[Post]] = client.sessionedWith { session =>
    client.postCollection
      .find(session, Filter.eq("username", username))
      .sort(Sort.desc("createdAt"))
      .all
      .map(_.toSeq)
  }.orDie

  override def loadPost(postId: PostId): IO[PostNotFound.type, Post] =
    client
      .sessionedWith { session =>
        client.postCollection.find(session, Filter.idEq(postId.value.toString)).first
      }
      .orDie
      .someOrFail(PostNotFound)

  override def likePost(
      userId: UserId,
      username: String,
      postId: PostId
  ): IO[PostNotFound.type | PostAlreadyLiked.type, Unit] =
    client
      .sessionedWith { session =>
        client.postCollection
          .updateOne(
            session,
            Filter.idEq(postId.value.toString),
            Update.set(s"likes.${userId.value}", username)
          )
      }
      .orDie
      .flatMap(result =>
        if result.getMatchedCount == 0 then ZIO.fail(PostNotFound)
        else if result.getModifiedCount == 0 then ZIO.fail(PostAlreadyLiked)
        else ZIO.unit
      )

  def unlikePost(userId: UserId, username: String, postId: PostId): IO[PostNotFound.type | PostNotLiked.type, Unit] =
    client
      .sessionedWith { session =>
        client.postCollection.updateOne(
          session,
          Filter.idEq(postId.value.toString),
          Update.unset(s"likes.${userId.value}")
        )
      }
      .orDie
      .flatMap(result =>
        if result.getMatchedCount == 0 then ZIO.fail(PostNotFound)
        else if result.getModifiedCount == 0 then ZIO.fail(PostNotLiked)
        else ZIO.unit
      )

object MongoPostDao:
  val layer: RLayer[AppMongoClient, MongoPostDao] = ZLayer.fromFunction(MongoPostDao(_))
