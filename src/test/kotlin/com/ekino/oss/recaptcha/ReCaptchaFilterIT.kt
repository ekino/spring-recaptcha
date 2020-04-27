package com.ekino.oss.recaptcha

import com.ekino.oss.recaptcha.config.BY_PASS_VALIDATION_HEADER_NAME
import com.ekino.oss.recaptcha.config.DEFAULT_RESPONSE_PARAM_NAME
import com.ekino.oss.recaptcha.service.ReCaptchaValidationService
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.http.Fault
import com.ninjasquad.springmockk.SpykBean
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
internal class ReCaptchaFilterIT(private val mockMvc: MockMvc) {

  @SpykBean
  private lateinit var reCaptchaValidationService: ReCaptchaValidationService

  @BeforeAll
  fun prepare() {
    prepareStub()
  }

  @Test
  fun `should successfully validate reCaptcha`() {
    val recaptchaResponse = "valid_recaptcha_response"
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 1) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should fail to validate reCaptcha when verification success is false`() {
    val recaptchaResponse = "invalid_recaptcha_response"
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isForbidden }
      content {
        json("""
        {
          "code": "recaptcha.validation.failed",
          "details": [
              "invalid-input-secret",
              "invalid-input-response"
          ],
          "message": "Validation failed for reCaptcha response."
      }
      """)
      }
    }
  }

  @Test
  fun `should fail to validate reCaptcha without response parameter`() {
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isBadRequest }
      content {
        json("""
        {
            "code": "recaptcha.missing.response",
            "message": "Unable to retrieve recaptcha response parameter. Please check configuration if endpoint really need reCaptcha validation or if response parameter name is correct."
        }
      """)
      }
    }
  }

  @Test
  fun `should fail to validate reCaptcha when verification request fail`() {
    val recaptchaResponse = "other_recaptcha_response"
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isForbidden }
      content {
        json("""
        {
          "code": "recaptcha.request.failed",
          "message": "Connection reset"
      }
      """)
      }
    }

    verify(exactly = 1) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should by-pass reCaptcha validation`() {
    val recaptchaResponse = "bypass_recaptcha_response"
    mockMvc.post("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
      header(BY_PASS_VALIDATION_HEADER_NAME, "by-pass-key")
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 0) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should not validate reCaptcha for GET method`() {
    val recaptchaResponse = "get_method_recaptcha_response"
    mockMvc.get("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 0) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should validate reCaptcha for PUT method`() {
    val recaptchaResponse = "valid_recaptcha_response"
    mockMvc.put("/test") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 1) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should validate reCaptcha for POST method with sub path`() {
    val recaptchaResponse = "valid_recaptcha_response"
    mockMvc.post("/test/id/sub-resources") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 1) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  @Test
  fun `should not validate reCaptcha for when url not filtered`() {
    val recaptchaResponse = "valid_recaptcha_response"
    mockMvc.post("/test/id/other-sub-resources") {
      accept = MediaType.APPLICATION_JSON
      header(DEFAULT_RESPONSE_PARAM_NAME, recaptchaResponse)
    }.andExpect {
      status { isNoContent }
    }

    verify(exactly = 0) { reCaptchaValidationService.validateReCaptcha(recaptchaResponse) }
  }

  private fun prepareStub() {
    stubFor(post(urlEqualTo("/siteverify?secret=secretKey&response=valid_recaptcha_response"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBodyFile("verify_recaptcha_ok.json")))

    stubFor(post(urlEqualTo("/siteverify?secret=secretKey&response=invalid_recaptcha_response"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBodyFile("verify_recaptcha_ko.json")))

    stubFor(post(urlEqualTo("/siteverify?secret=secretKey&response=other_recaptcha_response"))
      .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
  }
}
