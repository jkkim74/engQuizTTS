package com.lang.engquiztts.model

data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo", // 모델 설정 (또는 gpt-4)
    val messages: List<Message>,
    val max_tokens: Int = 100
)

data class Message(
    val role: String,  // "system", "user", "assistant" 중 하나
    val content: String
)

