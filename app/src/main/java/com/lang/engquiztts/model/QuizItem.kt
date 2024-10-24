package com.lang.engquiztts.model

data class QuizItem(val question: String, val answer: String)

val askedQuizzes = mutableSetOf<String>()  // 출제된 퀴즈 질문을 저장하는 Set
