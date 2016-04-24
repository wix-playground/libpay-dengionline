package com.wix.pay.dengionline.model

case class Response(code: Int,
                    message: String,
                    transaction_id: Option[String] = None,
                    external_id: Option[String] = None,
                    acquirer_id: Option[String] = None,
                    authcode: Option[String] = None)
