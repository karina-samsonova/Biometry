package com.example.biometry

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.biometry.databinding.DialogPhotoPreviewBinding


class PreviewDialog(
    private val photoUri: Uri
) : DialogFragment() {

    lateinit var binding: DialogPhotoPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPhotoPreviewBinding.bind(
            inflater.inflate(
                R.layout.dialog_photo_preview,
                container
            )
        )

        binding.photoPreview.setImageURI(photoUri)

        binding.buttonRetakePhoto.setOnClickListener { dismiss() }

        binding.buttonPostPhoto.setOnClickListener {
            Toast.makeText(requireContext(), photoUri.toString(), Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun getTheme() = R.style.SheetDialog

}