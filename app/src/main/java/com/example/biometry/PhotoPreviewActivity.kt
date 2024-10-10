package com.example.biometry

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biometry.databinding.ActivityCameraBinding
import com.example.biometry.databinding.ActivityPhotoPreviewBinding

class PhotoPreviewActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPhotoPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPhotoPreviewBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_photo_preview)

        val photoUri = Uri.parse(intent.getStringExtra("photoUri"))
        val photoPreview = findViewById<ImageView>(R.id.photoPreview)
        photoPreview.setImageURI(photoUri)

        val buttonRetakePhoto = findViewById<AppCompatButton>(R.id.buttonRetakePhoto)
        buttonRetakePhoto.setOnClickListener { finish() }

        val buttonPostPhoto = findViewById<AppCompatButton>(R.id.buttonPostPhoto)
        buttonPostPhoto.setOnClickListener {
            Toast.makeText(baseContext, photoUri.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "Biometry"

        fun newIntent(context: Context?, photoUri: Uri): Intent {
            val intent = Intent(context, PhotoPreviewActivity::class.java)
            intent.putExtra("photoUri", photoUri.toString())
            return intent
        }
    }
}