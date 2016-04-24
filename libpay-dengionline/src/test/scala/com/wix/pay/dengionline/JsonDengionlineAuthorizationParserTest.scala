package com.wix.pay.dengionline

import com.wix.pay.dengionline.DengionlineMatchers._
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class JsonDengionlineAuthorizationParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val authorizationParser: DengionlineAuthorizationParser = new JsonDengionlineAuthorizationParser

    val someAuthorization = DengionlineAuthorization(
      transactionId = "some transaction ID"
    )
  }

  "stringify and then parse" should {
    "yield an authorization similar to the original one" in new Ctx {
      val authorizationKey = authorizationParser.stringify(someAuthorization)
      authorizationParser.parse(authorizationKey) must beAuthorization(
        transactionId = ===(someAuthorization.transactionId)
      )
    }
  }
}
