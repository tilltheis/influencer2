package influencer2.http

import influencer2.http.{Login, SessionUser}
import influencer2.user.InvalidCredentials
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object AppJsonCodec:
  given JsonDecoder[Login] = DeriveJsonDecoder.gen
  given JsonEncoder[InvalidCredentials.type ] = JsonEncoder.string.contramap(_ => "invalid credentials")
  given JsonEncoder[SessionUser] = DeriveJsonEncoder.gen
end AppJsonCodec
