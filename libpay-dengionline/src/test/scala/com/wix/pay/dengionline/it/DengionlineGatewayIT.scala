package com.wix.pay.dengionline.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.dengionline.DengionlineMatchers._
import com.wix.pay.dengionline._
import com.wix.pay.dengionline.model.Errors
import com.wix.pay.dengionline.testkit.DengionlineDriver
import com.wix.pay.model.{CurrencyAmount, Customer, Deal, Payment}
import com.wix.pay.{PaymentErrorException, PaymentGateway, PaymentRejectedException}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class DengionlineGatewayIT extends SpecWithJUnit {
  val dengionlinePort = 10021

  val requestFactory = new NetHttpTransport().createRequestFactory()
  val driver = new DengionlineDriver(port = dengionlinePort)

  step {
    driver.startProbe()
  }

  sequential

  trait Ctx extends Scope {
    val merchantParser = new JsonDengionlineMerchantParser()
    val authorizationParser = new JsonDengionlineAuthorizationParser()

    val someMerchant = DengionlineMerchant(
      siteId = "some site ID",
      salt = "some salt"
    )
    val merchantKey = merchantParser.stringify(someMerchant)

    val someCurrencyAmount = CurrencyAmount("RUB", 33.3)
    val somePayment = Payment(someCurrencyAmount, 1)
    val someCreditCard = CreditCard(
      number = "4012888818888",
      expiration = YearMonth(2020, 12),
      additionalFields = Some(CreditCardOptionalFields.withFields(
        csc = Some("123"),holderName = Some("John Smith")

      ))
    )

    val someDeal = Deal(
      id = "some deal ID",
      title = Some("some deal title"),
      description = Some("some deal description")
    )

    val someCustomer = Customer(
      email = Some("example@example.org"),
      ipAddress = Some("2.2.2.2")
    )

    val someAuthorization = DengionlineAuthorization(
      transactionId = "some transaction ID"
    )
    val authorizationKey = authorizationParser.stringify(someAuthorization)

    val someCaptureAmount = 11.1

    val dengionline: PaymentGateway = new DengionlineGateway(
      requestFactory = requestFactory,
      endpointUrl = s"http://localhost:$dengionlinePort/",
      merchantParser = merchantParser,
      authorizationParser = authorizationParser
    )

    driver.resetProbe()
  }

  "sale request via DengiOnline gateway" should {
    "gracefully fail on invalid merchant key" in new Ctx {
      driver.aSaleFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) isForbidden()

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain(Errors.forbidden.code.toString) and contain(Errors.forbidden.message)
      }
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aSaleFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) returns someAuthorization.transactionId

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }

    "gracefully fail on rejected card" in new Ctx {
      driver.aSaleFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) isDeclined(someAuthorization.transactionId)

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(Errors.decline.code.toString) and contain(Errors.decline.message)
      }
    }

    "gracefully fail when 3DS is required" in new Ctx {
      driver.aSaleFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) requires3ds(someAuthorization.transactionId)

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(Errors.awaitingExternalConfirmation.code.toString) and contain(Errors.awaitingExternalConfirmation.message)
      }
    }
  }

  "authorize request via DengiOnline gateway" should {
    "gracefully fail on invalid merchant key" in new Ctx {
      driver.anAuthorizeFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) isForbidden()

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain(Errors.forbidden.code.toString) and contain(Errors.forbidden.message)
      }
    }

    "successfully yield an authorization key on valid request" in new Ctx {
      driver.anAuthorizeFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) returns someAuthorization.transactionId

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beASuccessfulTry(
        check = beAuthorizationKey(
          authorization = beAuthorization(
            transactionId = ===(someAuthorization.transactionId)
          )
        )
      )
    }

    "gracefully fail on rejected card" in new Ctx {
      driver.anAuthorizeFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) isDeclined(someAuthorization.transactionId)

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(Errors.decline.code.toString) and contain(Errors.decline.message)
      }
    }

    "gracefully fail when 3DS is required" in new Ctx {
      driver.anAuthorizeFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        card = someCreditCard,
        currencyAmount = someCurrencyAmount,
        deal = someDeal,
        customer = someCustomer
      ) requires3ds(someAuthorization.transactionId)

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(Errors.awaitingExternalConfirmation.code.toString) and contain(Errors.awaitingExternalConfirmation.message)
      }
    }
  }

  "capture request via DengiOnline gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aCaptureFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        amount = someCaptureAmount,
        transactionId = someAuthorization.transactionId
      ) returns someAuthorization.transactionId

      dengionline.capture(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey,
        amount = someCaptureAmount
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }
  }

  "voidAuthorization request via DengiOnline gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aVoidAuthorizationFor(
        siteId = someMerchant.siteId,
        salt = someMerchant.salt,
        transactionId = someAuthorization.transactionId
      ) returns someAuthorization.transactionId

      dengionline.voidAuthorization(
        merchantKey = merchantKey,
        authorizationKey = authorizationKey
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }
  }

  step {
    driver.stopProbe()
  }
}
