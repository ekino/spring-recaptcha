package com.ekino.oss.recaptcha.exception

open class ReCaptchaValidationException(
  val code: String,
  override val message: String,
  val details: List<String>? = null
) : Exception(message)

object MissingResponseException : ReCaptchaValidationException(
  code = "recaptcha.missing.response",
  message = "Unable to retrieve recaptcha response parameter. " +
    "Please check configuration if endpoint really need reCaptcha validation or if response parameter name is correct.")
