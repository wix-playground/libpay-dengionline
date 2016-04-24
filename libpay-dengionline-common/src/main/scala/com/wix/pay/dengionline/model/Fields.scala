package com.wix.pay.dengionline.model

object Fields {
  val action = "action"
  val siteId = "site_id"
  val amount = "amount"
  val currency = "currency"
  val externalId = "external_id"
  val customerIp = "customer_ip"
  val forwardedIp = "forwarded_ip"
  val sourceType = "source_type"
  val only3dsCards = "only_3ds_cards"
  val merchantData = "md"
  val signature = "signature"
  val sign = "sign"

  val cardExpMonth = "exp_month"
  val cardExpYear = "exp_year"
  val cardNumber = "card"
  val cardHolderName = "holder"
  val cardCvv = "cvv"

  val email = "email"
  val billingCity = "billing_city"
  val billingPostal = "billing_postal"
  val billingAddress = "billing_address"
  val billingCountry = "billing_country"
  val billingRegion = "billing_region"
  val billingPhone = "billing_phone"

  val userScreenResolution = "user_screen_resolution"
  val userDatetime = "user_datetime"
  val userTimezoneOffset = "user_timezone_offset"
  val userAgent = "user_agent"
  val acceptLanguage = "accept_language"
  val refererUrl = "referer_url"
  val clientInfoHash = "client_info_hash"

  val recurringRegister = "recurring_register"
  val recurringValidThru = "recurring_valid_thru"
  val recurringRegistrationId = "recurring_registration_id"

  val mpiReturnUrl = "mpi_return_url"

  val rememberCardForSiteLogin = "remember_card_for_site_login"
  val forceDisableCallback = "force_disable_callback"
  val firstCallbackDelay = "first_callback_delay"
  val transactionJobHours = "transaction_job_hours"
  val transactionJobType = "transaction_job_type"

  val cardServiceSegmentId = "card_service_segment_id"
  val cardServiceSegmentData = "card_service_segment_data"

  val transactionId = "transaction_id"
  val comment = "comment"
  val description = "description"
  val language = "language"
  val realAmount = "real_amount"
  val realCurrency = "real_currency"
  val paymentTypeId = "payment_type_id"
}
