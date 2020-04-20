package com.ekino.oss.recaptcha.client

import java.time.OffsetDateTime

/**
 * ReCaptcha response DTO.
 */
data class ReCaptchaResponseDto(
  val success: Boolean? = false,
  val challengeTs: OffsetDateTime? = null,
  val hostName: String? = null,
  val errorCodes: List<String> = emptyList()
)
