package com.wix.pay.dengionline

import java.math.{BigDecimal => JBigDecimal}

import com.wix.pay.creditcard.CreditCard
import com.wix.pay.dengionline.model.{Actions, Fields}
import com.wix.pay.model.CurrencyAmount

object DengionlineHelper {
  def createPurchaseRequest(merchant: DengionlineMerchant,
                            currencyAmount: CurrencyAmount,
                            card: CreditCard,
                            dealId: String,
                            customerIpAddress: Option[String] = None,
                            customerEmail: Option[String] = None): Map[String, String] = {
    createAuthorizationOrPurchaseRequest(
      action = Actions.purchase,
      merchant = merchant,
      currencyAmount = currencyAmount,
      card = card,
      dealId = dealId,
      customerIpAddress = customerIpAddress,
      customerEmail = customerEmail
    )
  }

  def createAuthorizationRequest(merchant: DengionlineMerchant,
                                 currencyAmount: CurrencyAmount,
                                 card: CreditCard,
                                 dealId: String,
                                 customerIpAddress: Option[String] = None,
                                 customerEmail: Option[String] = None): Map[String, String] = {
    createAuthorizationOrPurchaseRequest(
      action = Actions.authorization,
      merchant = merchant,
      currencyAmount = currencyAmount,
      card = card,
      dealId = dealId,
      customerIpAddress = customerIpAddress,
      customerEmail = customerEmail
    )
  }

  def createConfirmationRequest(merchant: DengionlineMerchant,
                                amount: Double,
                                authorization: DengionlineAuthorization): Map[String, String] = {
    val params = Map(
      Fields.action -> Actions.confirmation,
      Fields.siteId -> merchant.siteId,
      Fields.transactionId -> authorization.transactionId,
      Fields.amount -> toDengionlineAmount(amount)
    )

    val signature = Signer.calculateSignature(params, merchant.salt)
    params + (Fields.signature -> signature)
  }

  def createVoidRequest(merchant: DengionlineMerchant,
                        authorization: DengionlineAuthorization): Map[String, String] = {
    val params = Map(
      Fields.action -> Actions.void,
      Fields.siteId -> merchant.siteId,
      Fields.transactionId -> authorization.transactionId
    )

    val signature = Signer.calculateSignature(params, merchant.salt)
    params + (Fields.signature -> signature)
  }

  private def createAuthorizationOrPurchaseRequest(action: String,
                                                   merchant: DengionlineMerchant,
                                                   currencyAmount: CurrencyAmount,
                                                   card: CreditCard,
                                                   dealId: String,
                                                   customerIpAddress: Option[String] = None,
                                                   customerEmail: Option[String] = None): Map[String, String] = {
    val params = Map(
      Fields.action -> action,
      Fields.siteId -> merchant.siteId,
      Fields.amount -> toDengionlineAmount(currencyAmount.amount),
      Fields.currency -> currencyAmount.currency,
      Fields.externalId -> dealId,
      Fields.customerIp -> customerIpAddress.getOrElse("0.0.0.0"),
      Fields.cardExpMonth -> f"${card.expiration.month}%02d",
      Fields.cardExpYear -> f"${card.expiration.year}%04d",
      Fields.cardNumber -> card.number,
      Fields.cardHolderName -> card.holderName.get,
      Fields.cardCvv -> card.csc.get,
      Fields.email -> customerEmail.getOrElse(""),
      Fields.billingPostal -> card.billingPostalCode.getOrElse(""),
      Fields.billingAddress -> card.billingAddress.getOrElse("")
    )

    val signature = Signer.calculateSignature(params, merchant.salt)
    params + (Fields.signature -> signature)
  }

  private def toDengionlineAmount(amount: Double): String = {
    JBigDecimal.valueOf(amount).movePointRight(2).intValueExact().toString
  }
}
