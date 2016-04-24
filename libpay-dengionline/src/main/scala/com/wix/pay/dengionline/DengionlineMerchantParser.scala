package com.wix.pay.dengionline

trait DengionlineMerchantParser {
  def parse(merchantKey: String): DengionlineMerchant
  def stringify(merchant: DengionlineMerchant): String
}
