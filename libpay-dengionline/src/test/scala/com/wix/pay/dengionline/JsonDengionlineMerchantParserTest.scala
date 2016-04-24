package com.wix.pay.dengionline


import org.specs2.matcher.MustMatchers._
import org.specs2.matcher.{AlwaysMatcher, Matcher}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonDengionlineMerchantParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val merchantParser: DengionlineMerchantParser = new JsonDengionlineMerchantParser

    def beDengionlineMerchant(siteId: Matcher[String] = AlwaysMatcher(),
                              salt: Matcher[String] = AlwaysMatcher()): Matcher[DengionlineMerchant] = {
      siteId ^^ { (_: DengionlineMerchant).siteId aka "siteId" } and
        salt ^^ { (_: DengionlineMerchant).salt aka "salt" }
    }

    val someMerchant = DengionlineMerchant(
      siteId = "some site ID",
      salt = "some salt"
    )
  }

  "stringify and then parse" should {
    "yield a merchant similar to the original one" in new Ctx {
      val merchantKey = merchantParser.stringify(someMerchant)
      merchantParser.parse(merchantKey) must beDengionlineMerchant(
        siteId = ===(someMerchant.siteId),
        salt = ===(someMerchant.salt)
      )
    }
  }
}
