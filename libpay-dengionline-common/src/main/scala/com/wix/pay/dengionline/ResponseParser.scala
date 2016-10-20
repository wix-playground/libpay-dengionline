package com.wix.pay.dengionline

import com.wix.pay.dengionline.model.Response
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

object ResponseParser {
  private implicit val formats = DefaultFormats

  def parse(str: String): Response = {
    Serialization.read[Response](str)
  }

  def stringify(obj: Response): String = {
    Serialization.write(obj)
  }
}
