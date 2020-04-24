package com.ekino.oss.recaptcha.service

import com.ekino.oss.recaptcha.client.ReCaptchaClient
import com.ekino.oss.recaptcha.config.ReCaptchaProperties
import mu.KotlinLogging
import java.io.IOException

private val logger = KotlinLogging.logger {}

/**
 * Service to handle reCaptcha validation.
 * Call reCaptcha external service with required parameters.
 */
class ReCaptchaValidationService(private val reCaptchaClient: ReCaptchaClient, private val reCaptchaProperties: ReCaptchaProperties) {

  fun validateReCaptcha(reCaptchaResponse: String): ReCaptcaValidationResult {
    val result = try {
      reCaptchaClient.verifyReCaptcha(reCaptchaProperties.secret, reCaptchaResponse).execute().body()
    } catch (exception: IOException) {
      logger.error(exception) { "Request for reCaptcha validation failed." }
      return ReCaptcaValidationResult.Failure(
        code = "recaptcha.request.failed",
        message = exception.message ?: "ReCaptcha validation request failed."
      )
    }

    return if (result?.success == true) {
      ReCaptcaValidationResult.Success
    } else {
      ReCaptcaValidationResult.Failure(
        code = "recaptcha.validation.failed",
        message = "Validation failed for reCaptcha response.",
        details = result?.errorCodes
      )
    }
  }
}

sealed class ReCaptcaValidationResult {
  object Success : ReCaptcaValidationResult()
  data class Failure(val message: String, val code: String, var details: List<String>? = null) : ReCaptcaValidationResult()
}
