package com.ekino.oss.recaptcha.config

import com.ekino.oss.recaptcha.exception.MissingResponseException
import com.ekino.oss.recaptcha.exception.ReCaptchaValidationException
import com.ekino.oss.recaptcha.service.ReCaptcaValidationResult
import com.ekino.oss.recaptcha.service.ReCaptchaValidationService
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal const val BY_PASS_VALIDATION_HEADER_NAME = "X-ReCaptcha-ByPass-Key"

/**
 * Custom filter for validating reCaptcha.
 * Will retrieve reCaptcha response from header or request parameter from {@code responseName}.
 * Depending on configuration, validation will be applied only for specific method.
 * Validation can be by-passed passing a custom header with a specific key previously configured.
 */
class ReCaptchaFilter(
  private val validationService: ReCaptchaValidationService,
  private val responseName: String,
  private val byPassKey: String?,
  private val filteredMethods: Set<String>
) : OncePerRequestFilter() {

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    if (!byPassReCaptchaValidation(request) && requestMethodIsFiltered(request)) {
      logger.debug { "[ReCaptcha] ${request.requestURI} is protected by reCaptcha. ReCaptcha response will be validated." }

      val reCaptchaResponse = request.getParameter(responseName)
        ?: request.getHeader(responseName)
        ?: throw MissingResponseException

      when (val validation = validationService.validateReCaptcha(reCaptchaResponse)) {
        is ReCaptcaValidationResult.Failure -> throw ReCaptchaValidationException(
          code = validation.errorCode,
          message = validation.errorMessage,
          details = validation.errorDetails
        )
        is ReCaptcaValidationResult.Success -> logger.debug { "[ReCaptcha] Validation succeeded." }
      }
    }

    filterChain.doFilter(request, response)
  }

  private fun requestMethodIsFiltered(request: HttpServletRequest): Boolean =
    filteredMethods.contains(request.method)

  private fun byPassReCaptchaValidation(request: HttpServletRequest) =
    request.getHeader(BY_PASS_VALIDATION_HEADER_NAME) == byPassKey
}
