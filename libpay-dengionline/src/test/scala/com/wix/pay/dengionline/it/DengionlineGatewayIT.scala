package com.wix.pay.dengionline.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.dengionline.DengionlineMatchers._
import com.wix.pay.dengionline._
import com.wix.pay.dengionline.model.{Errors, Response}
import com.wix.pay.dengionline.testkit.DengionlineDriver
import com.wix.pay.model.{CurrencyAmount, Customer, Deal}
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

    def aPurchaseRequest = {
      DengionlineHelper.createPurchaseRequest(
        merchant = someMerchant,
        currencyAmount = someCurrencyAmount,
        card = someCreditCard,
        dealId = someDeal.id,
        customerIpAddress = someCustomer.ipAddress,
        customerEmail = someCustomer.email
      )
    }

    def anAuthorizationRequest = {
      DengionlineHelper.createAuthorizationRequest(
        merchant = someMerchant,
        currencyAmount = someCurrencyAmount,
        card = someCreditCard,
        dealId = someDeal.id,
        customerIpAddress = someCustomer.ipAddress,
        customerEmail = someCustomer.email
      )
    }

    def aForbiddenResponse = Response(
      code = Errors.forbidden.code,
      message = Errors.forbidden.message
    )

    val someAuthorization = DengionlineAuthorization(
      transactionId = "some transaction ID"
    )
    val authorizationKey = authorizationParser.stringify(someAuthorization)

    val someCaptureAmount = 11.1

    def aSuccessfulResponse = Response(
      code = Errors.success.code,
      message = Errors.success.message,
      transaction_id = Some(someAuthorization.transactionId)
    )

    def aDeclinedResponse = Response(
      code = Errors.decline.code,
      message = Errors.decline.message,
      transaction_id = Some(someAuthorization.transactionId)
    )

    def a3dsRequiredResponse = Response(
      code = Errors.awaitingExternalConfirmation.code,
      message = Errors.awaitingExternalConfirmation.message,
      transaction_id = Some(someAuthorization.transactionId)
    )

    def aConfirmationRequest = {
      DengionlineHelper.createConfirmationRequest(
        merchant = someMerchant,
        amount = someCaptureAmount,
        authorization = someAuthorization
      )
    }

    def aVoidRequest = {
      DengionlineHelper.createVoidRequest(
        merchant = someMerchant,
        authorization = someAuthorization
      )
    }

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
      driver.aRequestFor(aPurchaseRequest) returns aForbiddenResponse

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain(aForbiddenResponse.code.toString) and contain(aForbiddenResponse.message)
      }
    }

    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aRequestFor(aPurchaseRequest) returns aSuccessfulResponse

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beASuccessfulTry(
        check = ===(someAuthorization.transactionId)
      )
    }

    "gracefully fail on rejected card" in new Ctx {
      driver.aRequestFor(aPurchaseRequest) returns aDeclinedResponse

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(aDeclinedResponse.code.toString) and contain(aDeclinedResponse.message)
      }
    }

    "gracefully fail when 3DS is required" in new Ctx {
      driver.aRequestFor(aPurchaseRequest) returns a3dsRequiredResponse

      dengionline.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(a3dsRequiredResponse.code.toString) and contain(a3dsRequiredResponse.message)
      }
    }
  }

  "authorize request via DengiOnline gateway" should {
    "gracefully fail on invalid merchant key" in new Ctx {
      driver.aRequestFor(anAuthorizationRequest) returns aForbiddenResponse

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentErrorException => e.message must contain(aForbiddenResponse.code.toString) and contain(aForbiddenResponse.message)
      }
    }

    "successfully yield an authorization key on valid request" in new Ctx {
      driver.aRequestFor(anAuthorizationRequest) returns aSuccessfulResponse

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
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
      driver.aRequestFor(anAuthorizationRequest) returns aDeclinedResponse

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(aDeclinedResponse.code.toString) and contain(aDeclinedResponse.message)
      }
    }

    "gracefully fail when 3DS is required" in new Ctx {
      driver.aRequestFor(anAuthorizationRequest) returns a3dsRequiredResponse

      dengionline.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = someCurrencyAmount,
        customer = Some(someCustomer),
        deal = Some(someDeal)
      ) must beAFailedTry.like {
        case e: PaymentRejectedException => e.message must contain(a3dsRequiredResponse.code.toString) and contain(a3dsRequiredResponse.message)
      }
    }
  }

  "capture request via DengiOnline gateway" should {
    "successfully yield a transaction ID on valid request" in new Ctx {
      driver.aRequestFor(aConfirmationRequest) returns aSuccessfulResponse

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
      driver.aRequestFor(aVoidRequest) returns aSuccessfulResponse

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
