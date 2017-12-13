package com.wix.pay.dengionline.testkit


import scala.collection.JavaConversions._
import scala.collection.mutable
import java.util.{List => JList}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.google.api.client.http.UrlEncodedParser
import com.wix.e2e.http.api.StubWebServer
import com.wix.e2e.http.client.extractors.HttpMessageExtractors._
import com.wix.e2e.http.server.WebServerFactory.aStubWebServer
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.dengionline.model.{Errors, Response}
import com.wix.pay.dengionline.{DengionlineHelper, ResponseParser}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal}


class DengionlineDriver(port: Int,
                        defaultEmail: String = "example@example.org") {
  private val server: StubWebServer = aStubWebServer.onPort(port).build

  def start(): Unit = server.start()
  def stop(): Unit = server.stop()
  def reset(): Unit = server.replaceWith()


  def aSaleFor(siteId: String,
               salt: String,
               card: CreditCard,
               currencyAmount: CurrencyAmount,
               deal: Deal,
               customer: Customer): RequestCtx = {
    val request = DengionlineHelper.createPurchaseRequest(
      siteId = siteId,
      salt = salt,
      currencyAmount = currencyAmount,
      card = card,
      dealId = deal.id,
      customerIpAddress = customer.ipAddress,
      customerEmail = customer.email.getOrElse(defaultEmail))

    new RequestCtx(request)
  }

  def anAuthorizeFor(siteId: String,
                     salt: String,
                     card: CreditCard,
                     currencyAmount: CurrencyAmount,
                     deal: Deal,
                     customer: Customer): RequestCtx = {
    val request = DengionlineHelper.createAuthorizationRequest(
      siteId = siteId,
      salt = salt,
      currencyAmount = currencyAmount,
      card = card,
      dealId = deal.id,
      customerIpAddress = customer.ipAddress,
      customerEmail = customer.email.getOrElse(defaultEmail))

    new RequestCtx(request)
  }

  def aCaptureFor(siteId: String,
                  salt: String,
                  amount: Double,
                  transactionId: String): RequestCtx = {
    val request = DengionlineHelper.createConfirmationRequest(
      siteId = siteId,
      salt = salt,
      amount = amount,
      transactionId = transactionId)

    new RequestCtx(request)
  }

  def aVoidAuthorizationFor(siteId: String,
                            salt: String,
                            transactionId: String): RequestCtx = {
    val request = DengionlineHelper.createVoidRequest(
      siteId = siteId,
      salt = salt,
      transactionId = transactionId)

    new RequestCtx(request)
  }

  def aRequestFor(params: Map[String, String]): RequestCtx = {
    new RequestCtx(params)
  }


  class RequestCtx(params: Map[String, String]) {
    def returns(transactionId: String): Unit = {
      returns(Response(
        code = Errors.success.code,
        message = Errors.success.message,
        transaction_id = Some(transactionId)))
    }

    def getsForbidden(): Unit = {
      returns(Response(
        code = Errors.forbidden.code,
        message = Errors.forbidden.message))
    }

    def getsDeclined(transactionId: String): Unit = {
      returns(Response(
        code = Errors.decline.code,
        message = Errors.decline.message,
        transaction_id = Some(transactionId)))
    }

    def requires3ds(transactionId: String): Unit = {
      returns(Response(
        code = Errors.awaitingExternalConfirmation.code,
        message = Errors.awaitingExternalConfirmation.message,
        transaction_id = Some(transactionId)))
    }

    def returns(response: Response): Unit = {
      server.appendAll {
        case HttpRequest(
          HttpMethods.POST,
          Path("/"),
          _,
          entity,
          _) if isStubbedRequestEntity(entity) =>
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentType(MediaTypes.`application/json`), ResponseParser.stringify(response)))
      }
    }

    private def isStubbedRequestEntity(entity: HttpEntity): Boolean = {
      val requestParams = urlDecode(entity.extractAsString)
      params == requestParams
    }

    private def urlDecode(str: String): Map[String, String] = {
      val params = mutable.LinkedHashMap[String, JList[String]]()
      UrlEncodedParser.parse(str, mutableMapAsJavaMap(params))
      params.mapValues( _(0) ).toMap
    }
  }
}
