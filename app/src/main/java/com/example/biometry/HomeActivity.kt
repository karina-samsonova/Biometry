package com.example.biometry

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biometry.AssistantCameraActivity.Companion
import com.example.biometry.databinding.ActivityAssistantRegistrationBinding
import com.example.biometry.databinding.ActivityHomeBinding
import java.util.ArrayList
import java.util.Locale
import java.util.Objects

class HomeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var viewBinding: ActivityHomeBinding
    private val REQUEST_CODE_SPEECH_INPUT = 1

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        tts = TextToSpeech(this, this)

        viewBinding.buttonCancelMaxDialog.setOnClickListener {
            viewBinding.maxDialog.visibility = View.GONE
        }

        viewBinding.buttonSpeak.setOnClickListener {
            if (allPermissionsGranted()) {
                startSpeechToText()
            } else {
                requestPermissions()
            }
        }

        viewBinding.buttonBiometry.setOnClickListener {
            startActivity(RegistrationActivity.newIntent(this))
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale("RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ваш язык не поддерживается", Toast.LENGTH_SHORT).show()
            } else {
                askSomething(resources.getString(R.string.max_greeting))
            }
        }
    }

    private fun askSomething(text: String){
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}

            override fun onDone(utteranceId: String) {
                if (allPermissionsGranted()) {
                    startSpeechToText()
                } else {
                    requestPermissions()
                }
            }

            override fun onError(utteranceId: String) {}
        })
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
    }

    private fun speakOut(text: String){
        tts!!.setOnUtteranceProgressListener(null)
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, null, "")
    }

    private fun startSpeechToText(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                val resStr = Objects.requireNonNull(res)[0].lowercase()
                if (resStr.contains("ниче")) {
                    viewBinding.maxDialog.visibility = View.GONE
                } else if (resStr.contains("биометр")) {
                    startActivity(AssistantRegistrationActivity.newIntent(this))
                } else {
                    speakOut("Извините, мне не удалось распознать, что Вы сказали")
                }
            }
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    companion object {
        private const val TAG = "Biometry"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun newIntent(context: Context?): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }
}