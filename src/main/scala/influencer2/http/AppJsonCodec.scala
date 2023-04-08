package influencer2.http

import influencer2.http.{LoginRequest, SessionUser}
import influencer2.user.InvalidCredentials
import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec, JsonDecoder, JsonEncoder}

object AppJsonCodec:
  given JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen
  given JsonDecoder[LoginRequest] = DeriveJsonDecoder.gen
  given JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen
  given JsonCodec[SessionUser] = DeriveJsonCodec.gen
end AppJsonCodec
