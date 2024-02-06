package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityDeleteItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class DeleteItem : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteItemBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDeleteItemBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")
        val userType = intent.getStringExtra("userType")

        binding.delete.setOnClickListener{
            deleteItem(moduleId.toString(),userType.toString())
        }
        binding.cancel.setOnClickListener{
            var intent = Intent(this,ModuleItemView::class.java).also {
                it.putExtra("moduleId",moduleId)
                it.putExtra("userType","Teacher")
            }
            startActivity(intent)
        }

        readData(moduleId.toString())
    }

    private fun deleteItem(moduleId:String,userType:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Module").child(moduleId).removeValue().addOnSuccessListener {

            FirebaseDatabase.getInstance().getReference("Enroll")
                .orderByChild("moduleId")
                .equalTo(moduleId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for(childSnapshot in snapshot.children){
                                FirebaseDatabase.getInstance().getReference("Enroll").child(childSnapshot.key.toString()).removeValue()
                            }
                            var intent = Intent(this@DeleteItem,Dashboard::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        var intent = Intent(this@DeleteItem,Dashboard::class.java)
                        startActivity(intent)
                        finish()
                    }
                })
        }.addOnFailureListener{
            Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
        }
    }


    private fun readData(moduleId: String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
            if(it.exists()){
                binding.name.text = it.child("name").value.toString()
                binding.code.text = it.child("code").value.toString()
                binding.description.text = it.child("description").value.toString()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }
        }
    }

}