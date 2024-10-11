package com.example.biometry

import android.R.attr.animation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biometry.databinding.ActivityAssistantRegistrationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.coroutineContext


class AssistantRegistrationActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var viewBinding: ActivityAssistantRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAssistantRegistrationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale("RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ваш язык не поддерживается", Toast.LENGTH_SHORT).show()
            } else {
                firstPhrase()
            }
        }
    }

    private fun firstPhrase(){
        val text = resources.getText(R.string.max_first_phrase).toString()
        viewBinding.textViewMaxSpeech.text = text
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                val anim = AnimationUtils.loadAnimation(this@AssistantRegistrationActivity, R.anim.swining)
                viewBinding.imageViewMax.startAnimation(anim)
            }

            override fun onDone(utteranceId: String) {
                viewBinding.imageViewMax.clearAnimation()
                secondPhrase()
            }

            override fun onError(utteranceId: String) {}
        })
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
    }

    private fun secondPhrase(){
        val text = resources.getText(R.string.max_second_phrase).toString()
        CoroutineScope(Dispatchers.Main).launch {
            viewBinding.textViewMaxSpeech.text = text
            viewBinding.maxFrame.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(this@AssistantRegistrationActivity, R.anim.fade_in)
            viewBinding.maxFrame.startAnimation(anim)
        }
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}

            override fun onDone(utteranceId: String) { thirdPhrase() }

            override fun onError(utteranceId: String) {}
        })
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
    }

    private fun thirdPhrase(){
        val text = resources.getText(R.string.max_third_phrase).toString()
        CoroutineScope(Dispatchers.Main).launch {
            viewBinding.textViewMaxSpeech.text = text
            viewBinding.maxFrame.setImageDrawable(resources.getDrawable(R.drawable.max_frame_green))
        }
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}

            override fun onDone(utteranceId: String) {
                startActivity(AssistantCameraActivity.newIntent(this@AssistantRegistrationActivity))
                finish()
            }

            override fun onError(utteranceId: String) {}
        })
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
    }

    public override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        fun newIntent(context: Context?): Intent {
            return Intent(context, AssistantRegistrationActivity::class.java)
        }
    }

}