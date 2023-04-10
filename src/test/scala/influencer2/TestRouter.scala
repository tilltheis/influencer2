package influencer2

import influencer2.http.{AppRouter, JwtCodec, SessionController, UserController}
import influencer2.user.{MongoUserDao, UserService}
import zio.internal.macros.LayerMacros
import zio.{TaskLayer, ULayer, ZIO, ZLayer}

import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

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
      SessionController.layer,
      UserService.layer,
      MongoUserDao.layer,
      TestDatabase.layer
    )
