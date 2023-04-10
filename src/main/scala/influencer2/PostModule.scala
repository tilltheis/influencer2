package influencer2

import influencer2.post.{MongoPostDao, PostDao, PostService}
import influencer2.user.{MongoUserDao, UserDao, UserService}
import mongo4cats.zio.ZMongoDatabase
import zio.{RLayer, URLayer, ZEnvironment, ZIO, ZLayer}

object PostModule:
  val layer: RLayer[ZMongoDatabase, PostService & PostDao] =
    ZLayer.makeSome[ZMongoDatabase, PostService & PostDao](MongoPostDao.layer, PostService.layer)
