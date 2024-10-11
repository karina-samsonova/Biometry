package com.example.biometry

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

class PhotoAnalyzer(private val listener: PhotoListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        //TODO: implement server interaction
        CoroutineScope(Dispatchers.Main).launch {
            delay(4000)
            listener(false, "Проверьте освещение, чтобы лицо было четко видно", image)
            delay(4000)
            listener(false, "Пожалуйста, убедитесь, что ваше лицо находится в рамке", image)
            delay(4000)
            listener(true, "Фото успешно сделано", image)
            image.close()
        }
    }
}