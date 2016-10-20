package com.wix.pay.dengionline

import java.math.{BigDecimal => JBigDecimal}

import com.wix.pay.creditcard.CreditCard
import com.wix.pay.dengionline.model.{Actions, Fields}
import com.wix.pay.model.CurrencyAmount

object DengionlineHelper {
  def createPurchaseRequest(siteId: String,
                            salt: String,
                            currencyAmount: CurrencyAmount,
                            card: CreditCard,
                            dealId: String,
                            customerIpAddress: Option[String] = None,
                            customerEmail: String): Map[String, String] = {
    createAuthorizationOrPurchaseRequest(
      action = Actions.purchase,
      siteId = siteId,
      salt = salt,
      currencyAmount = currencyAmount,
      card = card,
      dealId = dealId,
      customerIpAddress = customerIpAddress,
      customerEmail = customerEmail
    )
  }

  def createAuthorizationRequest(siteId: String,
                                 salt: String,
                                 currencyAmount: CurrencyAmount,
                                 card: CreditCard,
                                 dealId: String,
                                 customerIpAddress: Option[String] = None,
                                 customerEmail: String): Map[String, String] = {
    createAuthorizationOrPurchaseRequest(
      action = Actions.authorization,
      siteId = siteId,
      salt = salt,
      currencyAmount = currencyAmount,
      card = card,
      dealId = dealId,
      customerIpAddress = customerIpAddress,
      customerEmail = customerEmail
    )
  }

  def createConfirmationRequest(siteId: String,
                                salt: String,
                                amount: Double,
                                transactionId: String): Map[String, String] = {
    val params = Map(
      Fields.action -> Actions.confirmation,
      Fields.siteId -> siteId,
      Fields.transactionId -> transactionId,
      Fields.amount -> toDengionlineAmount(amount)
    )

    val signature = Signer.calculateSignature(params, salt)
    params + (Fields.signature -> signature)
  }

  def createVoidRequest(siteId: String,
                        salt: String,
                        transactionId: String): Map[String, String] = {
    val params = Map(
      Fields.action -> Actions.void,
      Fields.siteId -> siteId,
      Fields.transactionId -> transactionId
    )

    val signature = Signer.calculateSignature(params, salt)
    params + (Fields.signature -> signature)
  }

  private def createAuthorizationOrPurchaseRequest(action: String,
                                                   siteId: String,
                                                   salt: String,
                                                   currencyAmount: CurrencyAmount,
                                                   card: CreditCard,
                                                   dealId: String,
                                                   customerIpAddress: Option[String] = None,
                                                   customerEmail: String): Map[String, String] = {
    val params = Map(
      Fields.action -> action,
      Fields.siteId -> siteId,
      Fields.amount -> toDengionlineAmount(currencyAmount.amount),
      Fields.currency -> currencyAmount.currency,
      Fields.externalId -> dealId,
      Fields.customerIp -> customerIpAddress.getOrElse("0.0.0.0"),
      Fields.cardExpMonth -> f"${card.expiration.month}%02d",
      Fields.cardExpYear -> f"${card.expiration.year}%04d",
      Fields.cardNumber -> card.number,
      Fields.cardHolderName -> card.holderName.get,
      Fields.cardCvv -> card.csc.get,
      Fields.email -> customerEmail,
      Fields.billingPostal -> card.billingPostalCode.getOrElse(""),
      Fields.billingAddress -> card.billingAddress.getOrElse("")
    )

    val signature = Signer.calculateSignature(params, salt)
    params + (Fields.signature -> signature)
  }

  private def toDengionlineAmount(amount: Double): String = {
    JBigDecimal.valueOf(amount).movePointRight(2).intValueExact().toString
  }
}
