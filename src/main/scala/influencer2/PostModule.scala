package influencer2

import influencer2.post.{MongoPostDao, PostDao, PostService}
import influencer2.user.{MongoUserDao, UserDao, UserService}
import mongo4cats.zio.{ZMongoClient, ZMongoDatabase}
import zio.{RLayer, URLayer, ZEnvironment, ZIO, ZLayer}

object PostModule:
  val layer: RLayer[ZMongoClient & ZMongoDatabase & UserDao, PostService & PostDao] =
    ZLayer
      .makeSome[ZMongoClient & ZMongoDatabase & UserDao, PostService & PostDao](MongoPostDao.layer, PostService.layer)
