package com.lang.engquiztts

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lang.engquiztts.model.Message
import com.lang.engquiztts.model.OpenAIRequest
import com.lang.engquiztts.model.OpenAIResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var userAnswerText: TextView
    private lateinit var feedbackText: TextView
    private val SPEECH_REQUEST_CODE = 0
    private val topics = listOf("animals", "colors", "fruits", "numbers", "shapes", "weather", "family")  // 퀴즈 주제 목록
    private var currentQuiz: String? = null  // 현재 퀴즈 질문 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
        userAnswerText = findViewById(R.id.userAnswerText)
        feedbackText = findViewById(R.id.feedbackText)

        findViewById<Button>(R.id.startQuizButton).setOnClickListener {
            startQuizForChildren()  // 퀴즈 시작
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
        }

        // UtteranceProgressListener 설정
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // TTS가 시작될 때 처리할 로직 (필요시 추가)
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "QUIZ_ID") {  // TTS에서 지정한 utteranceId와 동일한지 확인
                    startSpeechToText()  // TTS가 완료되면 STT 시작
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e("TTS_ERROR", "Error during TTS")
            }
        })
    }

    // 랜덤 토픽 선택
    private fun getRandomTopic(): String {
        return topics.random()  // 랜덤으로 하나의 토픽 선택
    }

    // ChatGPT API로 퀴즈 생성 요청
    private fun requestQuizFromChatGPT(topic: String, onQuizReceived: (String?) -> Unit) {
        val messages = listOf(
            Message("user", "Please give me a simple English word quiz question in Korean about $topic that is suitable for preschool children, and provide the correct answer in English. without answer")
        )

        val request = OpenAIRequest(
            model = "gpt-4o",
            messages = messages
        )

        RetrofitInstance.api.getCompletion(request).enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                if (response.isSuccessful) {
                    val quizResponse = response.body()?.choices?.get(0)?.message?.content
                    quizResponse?.let {
                        onQuizReceived(it)
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.errorBody()}")
                    onQuizReceived(null)
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                Log.e("API_ERROR", "Failed to get response from API", t)
                onQuizReceived(null)
            }
        })
    }

    // ChatGPT API로 사용자 답변 확인 요청
    private fun checkAnswerWithChatGPT(quiz: String, userAnswer: String, onAnswerChecked: (String?) -> Unit) {
        val messages = listOf(
            Message("user", "퀴즈의 질문은 : $quiz 입니다. 아이의 대답은 : $userAnswer. 입니다. 답이 맞나요?")
        )

        val request = OpenAIRequest(
            model = "gpt-4o",
            messages = messages
        )

        RetrofitInstance.api.getCompletion(request).enqueue(object : Callback<OpenAIResponse> {
            override fun onResponse(call: Call<OpenAIResponse>, response: Response<OpenAIResponse>) {
                if (response.isSuccessful) {
                    val feedbackResponse = response.body()?.choices?.get(0)?.message?.content
                    feedbackResponse?.let {
                        onAnswerChecked(it)
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.errorBody()}")
                    onAnswerChecked(null)
                }
            }

            override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                Log.e("API_ERROR", "Failed to get response from API", t)
                onAnswerChecked(null)
            }
        })
    }

    // 퀴즈 시작
    private fun startQuizForChildren() {
        val topic = getRandomTopic()  // 랜덤 토픽 선택
        requestQuizFromChatGPT(topic) { quizResponse ->
            if (quizResponse != null) {
                currentQuiz = quizResponse  // 현재 퀴즈 저장

                // TTS로 퀴즈 질문하기
                val utteranceId = "QUIZ_ID"
                // TTS로 퀴즈 질문하기
                tts.speak(quizResponse, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            } else {
                // 실패 시 대체 메시지
                tts.speak("Sorry, I couldn't find a quiz. Please try again.", TextToSpeech.QUEUE_FLUSH, null, "")
            }
        }
    }

    // STT로 사용자 답변 받기
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREA)
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val userAnswer = results?.get(0) ?: ""
            userAnswerText.text = "Your Answer: $userAnswer"
            currentQuiz?.let { quiz ->
                checkAnswerWithChatGPT(quiz, userAnswer) { feedbackResponse ->
                    feedbackResponse?.let {
                        // TTS로 ChatGPT의 피드백 출력
                        tts.speak(it, TextToSpeech.QUEUE_FLUSH, null, "")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}



