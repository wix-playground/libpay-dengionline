package com.wix.pay.dengionline

import com.google.api.client.http._
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.dengionline.model.{DeclineError, Error, Errors, Response}
import com.wix.pay.model._
import com.wix.pay.{PaymentErrorException, PaymentException, PaymentGateway, PaymentRejectedException}

import scala.collection.JavaConversions
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object Endpoints {
  val production = "https://gate.ecommpay.com/card/json/"
  val test = "https://gate-sandbox.ecommpay.com/card/json/"
}

class DengionlineGateway(requestFactory: HttpRequestFactory,
                         connectTimeout: Option[Duration] = None,
                         readTimeout: Option[Duration] = None,
                         numberOfRetries: Int = 0,
                         endpointUrl: String = Endpoints.production,
                         defaultEmail: String = "example@example.org",
                         merchantParser: DengionlineMerchantParser = new JsonDengionlineMerchantParser,
                         authorizationParser: DengionlineAuthorizationParser = new JsonDengionlineAuthorizationParser) extends PaymentGateway {
  override def authorize(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      require(creditCard.csc.isDefined, "CSC is mandatory for DengiOnline")
      require(creditCard.holderName.isDefined, "Cardholder name is mandatory for DengiOnline")
      require(deal.isDefined, "Deal is mandatory for DengiOnline")
      require(payment.installments == 1, "DengiOnline does not support installments")

      val merchant = merchantParser.parse(merchantKey)

      val request = DengionlineHelper.createAuthorizationRequest(
        siteId = merchant.siteId,
        salt = merchant.salt,
        currencyAmount = payment.currencyAmount,
        card = creditCard,
        dealId = deal.get.id,
        customerEmail = customer.flatMap { _.email }.getOrElse(defaultEmail),
        customerIpAddress = if (customer.isDefined) customer.get.ipAddress else None
      )
      val responseJson = doRequest(request)
      val response = ResponseParser.parse(responseJson)

      authorizationParser.stringify(DengionlineAuthorization(
        transactionId = extractTransactionId(response)
      ))
    } match {
      case Success(authorizationKey) => Success(authorizationKey)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(new PaymentErrorException(e.getMessage, e))
    }
  }

  override def capture(merchantKey: String, authorizationKey: String, amount: Double): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      val request = DengionlineHelper.createConfirmationRequest(
        siteId = merchant.siteId,
        salt = merchant.salt,
        amount = amount,
        transactionId = authorization.transactionId
      )
      val responseJson = doRequest(request)
      val response = ResponseParser.parse(responseJson)

      extractTransactionId(response)
    } match {
      case Success(transactionId) => Success(transactionId.toString)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(new PaymentErrorException(e.getMessage, e))
    }
  }

  override def sale(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    Try {
      require(creditCard.csc.isDefined, "CSC is mandatory for DengiOnline")
      require(creditCard.holderName.isDefined, "Cardholder name is mandatory for DengiOnline")
      require(deal.isDefined, "Deal is mandatory for DengiOnline")
      require(payment.installments == 1, "DengiOnline does not support installments")

      val merchant = merchantParser.parse(merchantKey)

      val request = DengionlineHelper.createPurchaseRequest(
        siteId = merchant.siteId,
        salt = merchant.salt,
        currencyAmount = payment.currencyAmount,
        card = creditCard,
        dealId = deal.get.id,
        customerEmail = customer.flatMap { _.email }.getOrElse(defaultEmail),
        customerIpAddress = if (customer.isDefined) customer.get.ipAddress else None
      )
      val responseJson = doRequest(request)
      val response = ResponseParser.parse(responseJson)

      extractTransactionId(response)
    } match {
      case Success(transactionId) => Success(transactionId.toString)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(new PaymentErrorException(e.getMessage, e))
    }
  }

  override def voidAuthorization(merchantKey: String, authorizationKey: String): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      val request = DengionlineHelper.createVoidRequest(
        siteId = merchant.siteId,
        salt = merchant.salt,
        transactionId = authorization.transactionId
      )
      val responseJson = doRequest(request)
      val response = ResponseParser.parse(responseJson)

      extractTransactionId(response)
    } match {
      case Success(transactionId) => Success(transactionId.toString)
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(new PaymentErrorException(e.getMessage, e))
    }
  }

  private def extractTransactionId(response: Response): String = {
    Error(code = response.code, message = response.message) match {
      case Errors.success => response.transaction_id.get
      case DeclineError(declineError) => throw PaymentRejectedException(declineError.toString)
      case error => throw PaymentErrorException(error.toString)
    }
  }

  private def doRequest(params: Map[String, String]): String = {
    val httpRequest = requestFactory.buildPostRequest(
      new GenericUrl(endpointUrl),
      new UrlEncodedContent(JavaConversions.mapAsJavaMap(params))
    )

    connectTimeout foreach (to => httpRequest.setConnectTimeout(to.toMillis.toInt))
    readTimeout foreach (to => httpRequest.setReadTimeout(to.toMillis.toInt))
    httpRequest.setNumberOfRetries(numberOfRetries)

    httpRequest.setThrowExceptionOnExecuteError(false)

    extractAndCloseResponse(httpRequest.execute())
  }

  private def extractAndCloseResponse(httpResponse: HttpResponse): String = {
    try {
      httpResponse.parseAsString()
    } finally {
      httpResponse.ignore()
    }
  }
}
