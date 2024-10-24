package com.lang.engquiztts
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val OPENAI_API_KEY = "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"

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // API 키를 Authorization 헤더로 추가하는 Interceptor
    private val authInterceptor = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")  // Authorization 헤더에 API 키 추가
            .build()
        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)  // API 키를 추가하는 Interceptor 사용
        .addInterceptor(interceptor)      // 로깅 Interceptor (디버깅 용도)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")  // OpenAI API 기본 URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OpenAIService by lazy {
        retrofit.create(OpenAIService::class.java)
    }
}
