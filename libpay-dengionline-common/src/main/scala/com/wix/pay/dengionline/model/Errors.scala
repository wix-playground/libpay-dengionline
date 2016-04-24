package com.wix.pay.dengionline.model

case class Error(code: Int, message: String)

/**
 * Taken from API documentation.
 * According to DengiOnline, errors with same code must be differentiated based on message.
 */
object Errors {
  /** Operation is completed successfully. */
  val success = Error(0, "Success.")
  /** The operation is not yet completed (see the description of corresponding operation). */
  val awaitingExternalConfirmation = Error(50, "Awaiting external confirmation.")

  /** Internal error. */
  val systemMalfunction = Error(1, "System malfunction.")
  /** Access to the operation is forbidden. */
  val forbidden = Error(2, "Forbidden.")
  /** Incorrect request signature. */
  val invalidSignature = Error(3, "Invalid signature.")
  /** Transaction was declined by the issuing bank. Clarifications should be received in the bank of a customer. */
  val decline = Error(100, "Operation was declined.")
  /** The transaction is currently being processed in parallel query, try again in 1-3 seconds. */
  val lockedTransaction = Error(101, "Locked transaction.")
  /** Order is currently being processed in parallel query, try again in 1-3 seconds. */
  val lockedOrder = Error(102, "Locked order.")
  /** Order has expired, see Orders. */
  val orderExpired = Error(103, "Order expired.")
  /* Successful operation has already passed by this order, see Orders */
  val orderAlreadyCompleted = Error(104, "Order already completed.")
  /** Transaction is not found. */
  val initialTransactionWasNotFound = Error(105, "Initial transaction was not found.")
  /** Incorrect value of parameter 'action'. */
  val invalidAction = Error(106, "Invalid action.")
  /** Incorrect value of parameter 'site'. */
  val invalidSite = Error(107, "Invalid site.")
  /** Incorrect value of parameter 'card'. */
  val invalidCardNumber = Error(108, "Invalid card number.")
  /** Incorrect value of parameter 'cvv'. */
  val invalidCvv = Error(109, "Invalid cvv.")
  /** Incorrect value of parameter 'holder'. */
  val invalidHolder = Error(110, "Invalid holder.")
  /** Incorrect value of parameter 'exp_month'. */
  val invalidExpirationMonth = Error(111, "Invalid expiration month.")
  /** Incorrect value of parameter 'exp_year'. */
  val invalidExpirationYear = Error(112, "Invalid expiration year.")
  /** Payment tool is switched off or is unavailable for the operation. */
  val unsupportedPaymentType = Error(113, "Unsupported payment type.")
  /** Check external_id param. */
  val invalidExternalId = Error(114, "Invalid external id.")
  /** Incorrect value of parameter 'amount'. */
  val invalidAmount = Error(115, "Invalid amount.")
  /** Incorrect value of parameter 'billing_city'. */
  val invalidCity = Error(116, "Invalid city.")
  /** Incorrect value of parameter 'billing_region'. */
  val invalidRegion = Error(117, "Invalid region.")
  /** Incorrect value of parameter 'billing_country'. */
  val invalidCountry = Error(118, "Invalid country.")
  /** Incorrect value of parameter 'billing_postal'. */
  val invalidPostalCode = Error(119, "Invalid postal code.")
  /** Incorrect value of parameter 'email'. */
  val invalidEmail = Error(120, "Invalid email.")
  /** Incorrect value of parameter 'external_id'. */
  val invalidOrder = Error(121, "Invalid order.")
  /** Incorrect value of parameter 'comment'. */
  val invalidComment = Error(122, "Invalid comment.")
  /** Required parameters 'transaction_id' or 'external_id' (one of them) has not been transferred. */
  val transactionIdOrExternalIdRequired = Error(124, "Transaction id or external id required.")
  /** Incorrect value of parameter 'best before date'. */
  val invalidBestBeforeDate = Error(125, "Invalid best before date.")
  /** Incorrect value of parameter 'phone'. */
  val invalidPhone = Error(126, "Invalid phone.")
  /** Incorrect value of parameter 'billing_address'. */
  val invalidAddress = Error(127, "Invalid address.")
  /** Incorrect value of parameter 'pa_res'. */
  val invalidPayerAuthenticationResult = Error(128, "Invalid payer authentication result.")
  /** For example, an attempt to send an operation 'Void' on confirmed 'Authorization', this is not correct. */
  val incorrectActionSequence = Error(130, "Incorrect action sequence.")
  /** Incorrect value of parameter 'user_datetime'. */
  val invalidDate = Error(137, "Invalid date.")
  /** Incorrect value of parameter 'customer_ip'. */
  val invalidCustomerIp = Error(138, "Invalid customer ip.")
  /** An incorrect response format of the external system. */
  val malformedAcquirerResponse = Error(144, "Malformed acquirer response")

  /** Transaction was declined by the issuing bank. Clarifications should be received in the bank of a customer.  */
  val declineGenenral = Error(100, "Decline (general, no comments)")
  /**
   * Fault on the side of the bank. Service is not available. Need to try again. When repeated - contact the bank through
   * which the transfer is carried out, or the customer's bank.
   */
  val declineCardIssuerOrSwitchInoperative = Error(907, "Decline reason message: card issuer or switch inoperative")
  /**
   * Invalid operation on the side of the bank.
   * Reasons need to be inquired in the bank through which the payment is carried out.
   */
  val declineInvalidTransaction = Error(902, "Decline reason message: invalid transaction")
  /**
   * Transaction is not found on the side of the bank. Possibly incorrect parameters are specified for a refund.
   * Clarifications are necessary to be obtained in the bank through which the payment is carried out.
   */
  val declineNotAbleToTraceBackToOriginalTransaction = Error(914, "Decline reason message: not able to trace back to original transaction")
  /**
   * System error on the side of the bank. Need to try again.
   * When repeated - contact the bank through which the payment is carried out.
   */
  val declineSystemMalfunction = Error(909, "Decline reason message: system malfunction")
  /** Exceeded limits on card transactions. The customer needs to contact his/her bank. */
  val declineExceedsWithdrawlAmountLimit = Error(121, "Decline, exceeds withdrawal amount limit")
  /** Expired card. The customer needs to contact the bank. */
  val declineExpiredCard = Error(101, "Decline, expired card")
  /** Invalid card number. The customer needs to contact its bank. */
  val declineInvalidCardNumber = Error(111, "Decline, invalid card number")
  /** Insufficient funds. The customer needs to contact its bank. */
  val declineNotSufficientFunds = Error(116, "Decline, not sufficient funds")
  /** Rejection by the issuing bank. Clarifications is necessary to be obtained in the bank of a customer. */
  val declineReferToCardIssuer = Error(107, "Decline, refer to card issuer")
  /**
   * For security reasons, this operation can not be completed successfully.
   * Clarifications need to be inquire in a bank of the customer.
   */
  val declineSecurityViolation = Error(122, "Decline, security violation")
  /** The transaction is not allowed to the card holder. The customer needs to contact its bank. */
  val declineTransactionNotPermittedToCardholder = Error(119, "Decline, transaction not permitted to cardholder")
}

object DeclineError {
  private val declineCodes = Set(
    Errors.decline,
    Errors.declineGenenral,
    Errors.declineCardIssuerOrSwitchInoperative,
    Errors.declineInvalidTransaction,
    Errors.declineNotAbleToTraceBackToOriginalTransaction,
    Errors.declineSystemMalfunction,
    Errors.declineExceedsWithdrawlAmountLimit,
    Errors.declineExpiredCard,
    Errors.declineInvalidCardNumber,
    Errors.declineNotSufficientFunds,
    Errors.declineReferToCardIssuer,
    Errors.declineSecurityViolation,
    Errors.declineTransactionNotPermittedToCardholder,
    Errors.awaitingExternalConfirmation // We treat mandatory 3DS as a decline
  )

  def unapply(error: Error): Option[Error] = {
    if (declineCodes.contains(error)) Some(error) else None
  }
}
