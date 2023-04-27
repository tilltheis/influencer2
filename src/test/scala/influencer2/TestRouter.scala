package influencer2

import influencer2.application.{AppRouter, JwtCodec, PostController, SessionController, UserController}
import influencer2.domain.{PostService, UserService}
import influencer2.infrastructure.{MongoPostDao, MongoUserDao}
import zio.internal.macros.LayerMacros
import zio.{TaskLayer, ZIO, ZLayer}

import javax.crypto.KeyGenerator

object TestRouter:
  val layer: TaskLayer[AppRouter] =
    val secretKeyLayer = ZLayer(ZIO.attempt {
      val keyGenerator = KeyGenerator.getInstance("HmacSHA256")
      keyGenerator.init(512)
      keyGenerator.generateKey()
    })

    ZLayer.make[AppRouter](
      AppRouter.layer,
      JwtCodec.layer,
      secretKeyLayer,
      UserController.layer,
      UserService.layer,
      MongoUserDao.layer,
      SessionController.layer,
      PostController.layer,
      PostService.layer,
      MongoPostDao.layer,
      TestDatabase.layer
    )
