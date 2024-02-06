package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityViewMaterialBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ViewMaterial : AppCompatActivity() {
    private lateinit var binding:ActivityViewMaterialBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewMaterialBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val materialId = intent.getStringExtra("materialId")
        val moduleId = intent.getStringExtra("moduleId")
        val userType = intent.getStringExtra("userType")

        if(userType.toString() == "Student"){
            binding.delete.visibility = View.GONE
        }else{
            binding.delete.visibility = View.VISIBLE
        }

        readData(materialId.toString())

        binding.download.setOnClickListener{
            download(materialId.toString())
        }
        binding.delete.setOnClickListener{
            deleteMat(materialId.toString(),moduleId.toString(),userType.toString())
        }
    }

    private fun readData(moduleId:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Material").child(moduleId).get().addOnSuccessListener {
            if(it.exists()){
                binding.name.text = it.child("name").value.toString()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }else{
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun download(materialId:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        val storageRef = FirebaseStorage.getInstance().reference
        val videoRef = storageRef.child("videos/$materialId.mp4")

        val materialName = binding.name.text.toString()


// Download the video file to a local file
        val localFile = File.createTempFile("video", "mp4")
        videoRef.getFile(localFile)
            .addOnSuccessListener {
                // Once the video file has been downloaded, add it to the device's media library
                val resolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$materialId.mp4")
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                }
                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Copy the downloaded video file to the media library
                resolver.openOutputStream(uri!!)?.use { outputStream ->
                    localFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // Notify the user that the download is complete
                Toast.makeText(applicationContext, "Video downloaded and added to gallery", Toast.LENGTH_SHORT).show()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }
            .addOnFailureListener {
                // If the download fails, notify the user
                Toast.makeText(applicationContext, "Download failed: ${it.message}", Toast.LENGTH_SHORT).show()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }
    }

    private fun deleteMat(materialId: String,moduleId: String,userType:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Material").child(materialId).removeValue().addOnSuccessListener {


                    // Video was successfully deleted
                    var intent = Intent(this,ModuleItemView::class.java).also {
                        it.putExtra("moduleId",moduleId)
                        it.putExtra("userType",userType)
                    }
                    startActivity(intent)
                    finish()


        }.addOnFailureListener{
            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
        }
    }
}