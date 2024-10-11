package com.example.biometry

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biometry.databinding.ActivityMainBinding
import com.example.biometry.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_registration)

        val buttonTakePhoto = findViewById<AppCompatButton>(R.id.buttonTakePhoto)
        buttonTakePhoto.setOnClickListener {
            val intent: Intent = CameraActivity.newIntent(this)
            startActivity(intent)
        }

        val buttonCancel = findViewById<TextView>(R.id.buttonCancel)
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context?): Intent {
            return Intent(context, RegistrationActivity::class.java)
        }
    }
}