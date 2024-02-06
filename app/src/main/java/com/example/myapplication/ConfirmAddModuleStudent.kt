package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.myapplication.databinding.ActivityConfirmAddModuleStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.UUID

class ConfirmAddModuleStudent : AppCompatActivity() {
    private lateinit var binding:ActivityConfirmAddModuleStudentBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConfirmAddModuleStudentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")
        val studentId = intent.getStringExtra("studentId")

        readData(studentId.toString())

        binding.confirm.setOnClickListener{
            addEnrolment(moduleId.toString(),studentId.toString())
        }
        binding.cancel.setOnClickListener{
            var intent = Intent(this,AddModuleStudents::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
            finish()
        }

        binding.addStudents.setOnClickListener{
            var intent = Intent(this,AddModuleStudents::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
            finish()
        }
        binding.backToModule.setOnClickListener{
            var intent = Intent(this,ModuleItemView::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
                it.putExtra("userType","Teacher")
            }
            startActivity(intent)
            finish()
        }

    }

    private fun readData(studentId:String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.success.visibility = View.GONE

        FirebaseDatabase.getInstance().getReference("Student").child(studentId).get().addOnSuccessListener {
            if(it.exists()){

                binding.name.text = it.child("firstName").value.toString() + " " +it.child("lastName").value.toString()
                binding.nanasaId.text = it.child("nanasaId").value.toString()

                binding.dataLayout.visibility = View.VISIBLE
                binding.loaderLayout.visibility = View.GONE

            }
        }
    }

    private fun addEnrolment(moduleId:String,studentId: String){
        var enrollmentId = UUID.randomUUID()
        val enrollment = Enrollment(studentId,moduleId, enrollmentId.toString())
        FirebaseDatabase.getInstance().getReference("Enroll/$enrollmentId").setValue(enrollment).addOnSuccessListener {
            binding.dataLayout.visibility = View.GONE
            binding.loaderLayout.visibility = View.GONE
            binding.success.visibility = View.VISIBLE
        }
    }

}