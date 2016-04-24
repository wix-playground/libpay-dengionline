package com.wix.pay.dengionline

import java.security.MessageDigest

import com.wix.pay.dengionline.model.Fields

/**
 * From the document (sic.):
 *  1. Signature (sign) is the HEX representation of the SHA1-hash of a specially-formed line (string like
 *     "35d11d32141601a6c10703f06591a16bb20c41ef").
 *  2. Encoding of signatured string is UTF-8.
 *  3. All the query parameters are sorted in ascending order parameter names. The parameter names in lower case, and it
 *     is always the rows that meet the following regular expression: [a-z_]{1}[a-z0-9_]
 *  4. All settings will be drawn into a single string using the separator character between them.
 *  5. parameters are separated with semicolon ";".
 *  6. Each parameter is attached in the form of substring "should have param_name:param_value" where should have
 *     param_name - name parameter, param_value - the value of the parameter, followed by a colon ":" - internal separator.
 *  7. Parameters whose value is the empty string "" - skipped.
 *  8. If parameter value is an array, its elements are sorted in ascending order of their keys and drawn to the delimiter
 *     character. Elements of arrays (nested arrays) are ignored and the delimiter character is dont needed.
 *  9. In the of the line which is made using via separator character is appended salt (salt) site; if signatured string
 *     is empty, then the delimiter character before the salt of the site is not intended, however, this is a exceptional
 *     case, which in practice cannot be used.
 * 10. To avoid double signing parameters with names "sign" and "signature" are always excluded from the signature.
 */
object Signer {
  private val sha1 = MessageDigest.getInstance("SHA-1")
  private val ignoredFields = Set(Fields.sign, Fields.signature)

  def calculateSignature(params: Map[String, String], salt: String): String = {
    val paramsStr = params.toSeq
      .filterNot {
        case (name, value) => ignoredFields.contains(name) || value.isEmpty
      }
      .sorted
      .map {
        case (name, value) => s"$name:$value"
      }
      .mkString(";")

    val stringForSignature = s"$paramsStr;$salt"

    calculateSha1(stringForSignature)
  }

  private def calculateSha1(str: String): String = {
    val data = str.getBytes("UTF-8")
    val hash = sha1.digest(data)
    hash.map("%02x" format _).mkString
  }
}
