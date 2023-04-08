package influencer2.http

import influencer2.http.{LoginRequest, SessionUser}
import influencer2.user.InvalidCredentials
import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec, JsonDecoder, JsonEncoder}

object AppJsonCodec:
  given JsonDecoder[LoginRequest] = DeriveJsonDecoder.gen
  given JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen
  given JsonEncoder[InvalidCredentials.type ] = JsonEncoder.string.contramap(_ => "invalid credentials")
  given JsonCodec[SessionUser] = DeriveJsonCodec.gen
end AppJsonCodec
