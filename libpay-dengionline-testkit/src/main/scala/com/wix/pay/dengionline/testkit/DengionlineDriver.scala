package com.wix.pay.dengionline.testkit

import java.util.{List => JList}

import com.google.api.client.http.UrlEncodedParser
import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.dengionline.model.Response
import com.wix.pay.dengionline.{DengionlineHelper, ResponseParser}
import com.wix.pay.model.{CurrencyAmount, Customer, Deal}
import spray.http._

import scala.collection.JavaConversions._
import scala.collection.mutable

class DengionlineDriver(port: Int) {
  private val probe = new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler)
  private val responseParser = new ResponseParser

  def startProbe() {
    probe.doStart()
  }

  def stopProbe() {
    probe.doStop()
  }

  def resetProbe() {
    probe.handlers.clear()
  }

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
      customerEmail = customer.email
    )

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
      customerEmail = customer.email
    )

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
      transactionId = transactionId
    )

    new RequestCtx(request)
  }

  def aVoidAuthorizationFor(siteId: String,
                            salt: String,
                            transactionId: String): RequestCtx = {
    val request = DengionlineHelper.createVoidRequest(
      siteId = siteId,
      salt = salt,
      transactionId = transactionId
    )

    new RequestCtx(request)
  }

  def aRequestFor(params: Map[String, String]): RequestCtx = {
    new RequestCtx(params)
  }

  class RequestCtx(params: Map[String, String]) {
    def returns(response: Response) {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path("/"),
        _,
        entity,
        _) if isStubbedRequestEntity(entity) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(ContentType(MediaTypes.`application/json`), responseParser.stringify(response)))
      }
    }

    private def isStubbedRequestEntity(entity: HttpEntity): Boolean = {
      val requestParams = urlDecode(entity.asString)
      params == requestParams
    }

    private def urlDecode(str: String): Map[String, String] = {
      val params = mutable.LinkedHashMap[String, JList[String]]()
      UrlEncodedParser.parse(str, mutableMapAsJavaMap(params))
      params.mapValues( _(0) ).toMap
    }
  }
}
