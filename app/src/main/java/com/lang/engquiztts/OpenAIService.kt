package com.lang.engquiztts

import com.lang.engquiztts.model.OpenAIRequest
import com.lang.engquiztts.model.OpenAIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun getCompletion(@Body request: OpenAIRequest): Call<OpenAIResponse>
}
