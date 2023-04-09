package influencer2.http

import zio.{IO, UIO, URLayer, ZIO, ZLayer}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import scala.concurrent.duration.TimeUnit

class JwtCodec(jwtSigningKey: SecretKey):
  def encodeJwtIntoHeaderPayloadSignature(content: String, expiresAt: Instant): UIO[(String, String, String)] =
    zio.Clock.currentTime(TimeUnit.SECONDS).map { now =>
      val claim = JwtClaim(content, issuedAt = Some(now), expiration = Some(expiresAt.getEpochSecond))
      val Array(header, payload, signature) = Jwt.encode(claim, jwtSigningKey, JwtAlgorithm.HS256).split('.')
      (header, payload, signature)
    }

  def decodeJwtFromHeaderPayloadSignature(
      header: String,
      payload: String,
      signature: String
  ): IO[InvalidJwtFormat.type, String] = {
    zio.Clock
      .currentTime(TimeUnit.SECONDS)
      .map { now =>
        Jwt
          .decode(
            s"$header.$payload.$signature",
            jwtSigningKey,
            Seq(JwtAlgorithm.HS256),
            JwtOptions(expiration = false)
          )
          .filter(_.expiration.forall(_ > now))
          .map(_.content)
          .toOption
      }
      .someOrFail(InvalidJwtFormat)
  }
end JwtCodec

object InvalidJwtFormat

object JwtCodec:
  val layer: URLayer[SecretKey, JwtCodec] = ZLayer.fromFunction(JwtCodec(_))
