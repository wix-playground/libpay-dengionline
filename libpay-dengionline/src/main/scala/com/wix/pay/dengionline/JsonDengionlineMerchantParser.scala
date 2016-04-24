package com.wix.pay.dengionline

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonDengionlineMerchantParser() extends DengionlineMerchantParser {
  implicit val formats = DefaultFormats

  override def parse(merchantKey: String): DengionlineMerchant = {
    Serialization.read[DengionlineMerchant](merchantKey)
  }

  override def stringify(merchant: DengionlineMerchant): String = {
    Serialization.write(merchant)
  }
}
