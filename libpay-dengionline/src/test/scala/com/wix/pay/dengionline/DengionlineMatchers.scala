package com.wix.pay.dengionline

import org.specs2.matcher.{AlwaysMatcher, Matcher, Matchers}

trait DengionlineMatchers extends Matchers {
  def authorizationParser: DengionlineAuthorizationParser

  def beAuthorization(transactionId: Matcher[String] = AlwaysMatcher()): Matcher[DengionlineAuthorization] = {
    transactionId ^^ { (_: DengionlineAuthorization).transactionId aka "transactionId" }
  }

  def beAuthorizationKey(authorization: Matcher[DengionlineAuthorization]): Matcher[String] = {
    authorization ^^ { authorizationParser.parse(_: String) aka "parsed authorization"}
  }
}

object DengionlineMatchers extends DengionlineMatchers {
  override val authorizationParser = new JsonDengionlineAuthorizationParser()
}