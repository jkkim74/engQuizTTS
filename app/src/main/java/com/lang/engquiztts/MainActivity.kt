package com.lang.engquiztts

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var robotFace: LottieAnimationView
    private val SPEECH_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TTS 초기화
        tts = TextToSpeech(this, this)

        // 애니메이션 뷰 초기화
        robotFace = findViewById(R.id.robotFace)

        // 음성 말하기 버튼 설정
        findViewById<Button>(R.id.speakButton).setOnClickListener {
            speak("Hello, how can I assist you today?")
        }

        // 음성 듣기 버튼 설정
        findViewById<Button>(R.id.sttButton).setOnClickListener {
            startSpeechToText()
        }
    }

    // TTS 초기화 완료 콜백
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    // TTS로 말하기 기능
    private fun speak(text: String) {
        robotFace.playAnimation() // 애니메이션 시작
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() { //UtteranceProgressListener
            override fun onStart(utteranceId: String?) {
                // 말이 시작되면 애니메이션 재생
                runOnUiThread {
                    robotFace.playAnimation()
                }
            }

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    robotFace.pauseAnimation() // 말이 끝나면 애니메이션 멈춤
                }
            }

            override fun onError(utteranceId: String?) {
                // 오류 발생 시 처리할 작업
            }
        })
    }

    // STT 시작 (음성 인식)
    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
        startActivityForResult(intent, SPEECH_REQUEST_CODE)
    }

    // STT 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            // 사용자가 말한 텍스트를 로봇 얼굴과 함께 TTS로 다시 말하게 합니다.
            speak(spokenText)
        }
    }

    override fun onDestroy() {
        // TTS 종료 처리
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
