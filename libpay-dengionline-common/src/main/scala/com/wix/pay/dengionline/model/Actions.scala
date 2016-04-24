package com.wix.pay.dengionline.model

object Actions {
  /** Authorization */
  val authorization = "auth"

  /** Confirmation of authorized transaction */
  val confirmation = "confirm"

  /** Cancelation of authorized transaction */
  val void = "void"

  /** Purchase (auth+confirm) */
  val purchase = "purchase"

  /** Return funds back to holder after confirm or purchase request */
  val refund = "refund"

  /** Complete 3ds */
  val complete3ds = "complete3ds"

  /** Payout */
  val payout = "payout"

  /** Payout by token */
  val payoutByToken = "payout_by_token"

  /** MOTO authorization */
  val motoAuth = "moto_auth"

  /** MOTO purchase */
  val motoPurchase = "moto_purchase"

  /** Update transaction in External processing status (only for internal use) */
  val cardUpdateExternalProcessingStatus = "card_update_external_processing_status"

  /** Authorization with card data from given transaction */
  val authCardFromGivenTransaction = "auth_card_from_given_transaction"
}
