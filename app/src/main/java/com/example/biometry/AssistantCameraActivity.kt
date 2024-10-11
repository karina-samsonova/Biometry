package com.example.biometry

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.biometry.databinding.ActivityAssistantCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AssistantCameraActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var viewBinding: ActivityAssistantCameraBinding
    private lateinit var mediaPlayer: MediaPlayer
    private var tts: TextToSpeech? = null

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var userCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

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
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAssistantCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        tts = TextToSpeech(this, this)

        mediaPlayer = MediaPlayer.create(this, R.raw.bell)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.buttonImageCapture.setOnClickListener { takePhoto() }

        viewBinding.buttonCameraSelector.setOnClickListener { flipCamera() }

        viewBinding.buttonCancel.setOnClickListener { finish() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale("RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ваш язык не поддерживается", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createPhotoFile(applicationContext)

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (userCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let {
                        //startActivity(PhotoPreviewActivity.newIntent(this@CameraActivity, it))
                        val dialog = PreviewDialog(it)
                        dialog.show(supportFragmentManager, "")
                    }
                }
            }
        )
    }

    private fun createPhotoFile(context: Context): File {
        val outputDirectory = getOutputDirectory(context)
        return File(outputDirectory, photoFileName()).apply {
            parentFile?.mkdirs()
        }
    }

    private fun photoFileName() =
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"

    private fun getOutputDirectory(context: Context): File {
        val mediaDir = context.getExternalFilesDir(null)?.let {
            File(it, context.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    private fun flipCamera() {
        userCameraSelector = if (userCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
            CameraSelector.DEFAULT_BACK_CAMERA
        else
            CameraSelector.DEFAULT_FRONT_CAMERA
        startCamera()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PhotoAnalyzer { success, hint, photo ->
                        if (success) {
                            viewBinding.imageViewCameraFrame.setImageDrawable(
                                resources.getDrawable(
                                    R.drawable.camera_frame_green
                                )
                            )
                            viewBinding.analyzerHint.text = hint
                            mediaPlayer.setOnCompletionListener {
                                finish()
                            }
                            mediaPlayer.start()
                            cameraProvider.unbind(it)
                            //TODO: set photo to imageViewPreview
                        } else {
                            viewBinding.analyzerHint.text = hint
                            speakOut(hint)
                        }
                    })
                }

            val cameraSelector = userCameraSelector

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun speakOut(text: String){
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
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
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "Biometry"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun newIntent(context: Context?): Intent {
            return Intent(context, AssistantCameraActivity::class.java)
        }
    }
}