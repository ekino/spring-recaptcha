package com.ekino.oss.recaptcha.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.POST
import java.time.Duration

const val DEFAULT_RESPONSE_PARAM_NAME = "g-recaptcha-response"
private const val GOOGLE_RECAPTCHA_URL = "https://www.google.com/recaptcha/api/"
private const val TIMEOUT_DEFAULT_SECONDS = 5L
private val defaultTimeout = Duration.ofSeconds(TIMEOUT_DEFAULT_SECONDS)

@ConfigurationProperties(prefix = "security.recaptcha")
@ConstructorBinding
data class ReCaptchaProperties(
  val client: ClientProperties = ClientProperties(),
  val responseName: String = DEFAULT_RESPONSE_PARAM_NAME,
  val secret: String,
  val urlPatterns: Set<String> = emptySet(),
  val byPassKey: String? = null,
  val filteredMethods: Set<HttpMethod> = setOf(POST)
) {
  data class ClientProperties(
    val url: String = GOOGLE_RECAPTCHA_URL,
    val connectTimeout: Duration = defaultTimeout,
    val readTimeout: Duration = defaultTimeout,
    val writeTimeout: Duration = defaultTimeout
  )
}
