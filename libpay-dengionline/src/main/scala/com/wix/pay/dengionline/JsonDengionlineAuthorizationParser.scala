package com.wix.pay.dengionline

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonDengionlineAuthorizationParser() extends DengionlineAuthorizationParser {
  implicit val formats = DefaultFormats

  override def parse(authorizationKey: String): DengionlineAuthorization = {
    Serialization.read[DengionlineAuthorization](authorizationKey)
  }

  override def stringify(authorization: DengionlineAuthorization): String = {
    Serialization.write(authorization)
  }
}
