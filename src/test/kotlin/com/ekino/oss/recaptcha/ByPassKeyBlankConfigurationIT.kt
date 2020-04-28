package com.ekino.oss.recaptcha

import com.ekino.oss.recaptcha.config.BY_PASS_VALIDATION_HEADER_NAME
import com.ekino.oss.recaptcha.config.DEFAULT_RESPONSE_PARAM_NAME
import com.ekino.oss.recaptcha.service.ReCaptcaValidationResult
import com.ekino.oss.recaptcha.service.ReCaptchaValidationService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["security.recaptcha.by-pass-key= "])
@AutoConfigureMockMvc
internal class ByPassKeyBlankConfigurationIT(private val mockMvc: MockMvc) {

  @MockkBean
  private lateinit var reCaptchaValidationService: ReCaptchaValidationService

  @Test
  fun `should not by-pass reCaptcha validation as key is empty`() {
    every { reCaptchaValidationService.validateReCaptcha(any()) } returns ReCaptcaValidationResult.Success

    val recaptchaResponse = "recaptchaResponse"
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
      header(BY_PASS_VALIDATION_HEADER_NAME, "")
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 1) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }
}
