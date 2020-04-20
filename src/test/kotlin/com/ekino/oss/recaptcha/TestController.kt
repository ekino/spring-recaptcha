package com.ekino.oss.recaptcha

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val API_PATH = "/test"

@RestController
@RequestMapping(API_PATH)
class TestController {

  @GetMapping
  fun testReCaptchaValidationMethodGet() = ResponseEntity.noContent().build<Void>()

  @PostMapping
  fun testReCaptchaValidationMethodPost() = ResponseEntity.noContent().build<Void>()

  @PutMapping
  fun testReCaptchaValidationMethodPut() = ResponseEntity.noContent().build<Void>()
}
