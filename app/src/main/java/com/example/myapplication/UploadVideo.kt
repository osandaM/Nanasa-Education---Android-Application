package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.example.myapplication.databinding.ActivityUploadVideoBinding
import com.google.firebase.auth.FirebaseAuth

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UploadVideo : AppCompatActivity() {
    private lateinit var binding:ActivityUploadVideoBinding
    private lateinit var user:FirebaseAuth

    private val PICK_VIDEO_REQUEST = 1

    private var moduleIndex = ""
    private var materialName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUploadVideoBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")
        moduleIndex = moduleId.toString()

        binding.upload.setOnClickListener {
            selectVideo()
        }
    }

    private fun selectVideo() {
        var name = binding.name.text.toString()
        materialName = name
        if(name.isNotEmpty()){

            binding.dataLayout.visibility = View.GONE
            binding.loaderLayout.visibility = View.VISIBLE

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO)
        }else{
            Toast.makeText(this, "Enter the name to upload", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadVideo(videoUri: Uri) {

        val materialId = UUID.randomUUID()
        val storageRef = Firebase.storage.reference
        val videoRef = storageRef.child("videos/$materialId.mp4")
        val uploadTask = videoRef.putFile(videoUri)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var material = Material(materialName,materialId.toString(),moduleIndex)
                FirebaseDatabase.getInstance().getReference("Material").child(materialId.toString()).setValue(material).addOnSuccessListener {
                    var intent = Intent(this,ModuleItemView::class.java).also {
                        it.putExtra("moduleId",moduleIndex.toString())
                    }
                    startActivity(intent)
                    finish()
                }
            } else {
                // Video upload failed
                binding.dataLayout.visibility = View.VISIBLE
                binding.loaderLayout.visibility = View.GONE
                Toast.makeText(this, "Video upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == Activity.RESULT_OK && data != null) {
            val videoUri = data.data
            if (videoUri != null) {
                uploadVideo(videoUri)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_VIDEO = 1
    }
}