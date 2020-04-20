package com.ekino.oss.recaptcha.client

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit client for validating reCaptcha response with secret.
 */
interface ReCaptchaClient {

  @POST("siteverify")
  fun verifyReCaptcha(@Query("secret") secret: String, @Query("response") response: String): Call<ReCaptchaResponseDto>
}
