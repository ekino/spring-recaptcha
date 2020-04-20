package com.ekino.oss.recaptcha.config

import com.ekino.oss.recaptcha.client.ReCaptchaClient
import com.ekino.oss.recaptcha.service.ReCaptchaValidationService
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

private val logger = KotlinLogging.logger {}

/**
 * Configuration for reCaptcha filter.
 * Available only for a servlet-based application.
 * Disabled if {@code security.recaptcha.enabled} is false.
 */
@ConditionalOnWebApplication(type = SERVLET)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ReCaptchaProperties::class)
@ConditionalOnProperty(value = ["security.recaptcha.enabled"], havingValue = "true", matchIfMissing = true)
@Import(ReCaptchaValidationService::class)
class ReCaptchaConfiguration(private val reCaptchaProperties: ReCaptchaProperties) {

  private val objectMapper: ObjectMapper by lazy {
    jacksonObjectMapper().apply {
      setDefaultPropertyInclusion(JsonInclude.Include.NON_ABSENT)
      setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
      configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      findAndRegisterModules()
    }
  }

  /**
   * Filter registration, protecting only urls defined in properties.
   */
  @Bean
  fun reCaptchaFilter(reCaptchaValidationService: ReCaptchaValidationService) =
    FilterRegistrationBean<ReCaptchaFilter>().apply {
      filter = ReCaptchaFilter(
        reCaptchaValidationService,
        reCaptchaProperties.responseName,
        reCaptchaProperties.byPassKey,
        reCaptchaProperties.filteredMethods.map(HttpMethod::name).toSet()
      )
      reCaptchaProperties.urlPatterns?.let { urlPatterns = it }
    }.also {
      logger.info { "[ReCaptcha] Filter configured for url patterns ${it.urlPatterns}." }
    }

  @Bean
  fun reCaptchaClient(): ReCaptchaClient =
    with(reCaptchaProperties.client) {
      Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .client(
          OkHttpClient().newBuilder()
            .connectTimeout(connectTimeout)
            .readTimeout(readTimeout)
            .writeTimeout(writeTimeout)
            .build()
        )
        .build()
        .create(ReCaptchaClient::class.java)
    }
}
