package com.ekino.oss.recaptcha.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import javax.servlet.http.HttpServletResponse

/**
 * Service to handle any failure in reCaptcha validation process.
 *
 * For any custom failure handling, override this class with a {@code @Primary}.
 *
 * Example
 * <pre>
 * @Service
 * class CustomReCaptchaResponseFailure(objectMapper: ObjectMapper): ReCaptchaFailureService(objectMapper) {
 *    override fun handleMissingResponseParameter(response: HttpServletResponse) {
 *      super.handleMissingResponseParameter(response)
 *    }
 *
 *    override fun handleValidationFailure(validationFailure: ReCaptcaValidationResult.Failure, response: HttpServletResponse) {
 *      super.handleValidationFailure(validationFailure, response)
 *    }
 *  }
 * </pre>
 */
open class ReCaptchaFailureService(private val objectMapper: ObjectMapper) {

  /**
   * Handle missing parameter response.
   * Set a {@code 400} status with a default error body.
   *
   * Override this method to handle missing parameter failure.
   */
  open fun handleMissingResponseParameter(response: HttpServletResponse) {
    handleResponse(response, HttpStatus.BAD_REQUEST, MissingResponseParameterErrorBody)
  }

  /**
   * Handle missing parameter response.
   * Set a {@code 409} status with a default error body from reCaptcha validation.
   *
   * Override this method to handle reCaptcha validation failure.
   */
  open fun handleValidationFailure(validationFailure: ReCaptcaValidationResult.Failure, response: HttpServletResponse) {
    handleResponse(response, HttpStatus.FORBIDDEN, validationFailure)
  }

  /**
   * Utility method to write status and content to {@code HttpServletResponse}.
   */
  protected fun handleResponse(response: HttpServletResponse, status: HttpStatus, responseContent: Any) {
    response.status = status.value()
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.writer.apply {
      print(objectMapper.writeValueAsString(responseContent))
      flush()
    }
  }

  /**
   * Default missing parameter response object.
   */
  object MissingResponseParameterErrorBody {
    val code = "recaptcha.missing.response"
    val message = "Unable to retrieve recaptcha response parameter. " +
      "Please check configuration if endpoint really need reCaptcha validation or if response parameter name is correct."
  }
}
