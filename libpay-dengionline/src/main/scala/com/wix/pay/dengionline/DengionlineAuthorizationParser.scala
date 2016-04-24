package com.wix.pay.dengionline

trait DengionlineAuthorizationParser {
  def parse(authorizationKey: String): DengionlineAuthorization
  def stringify(authorization: DengionlineAuthorization): String
}
