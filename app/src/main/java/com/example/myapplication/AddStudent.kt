package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddStudent : AppCompatActivity() {
    private lateinit var binding:ActivityAddStudentBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.register.setOnClickListener{
            addStudent();
        }
        binding.dashboard.setOnClickListener{
            startActivity(Intent(this,Dashboard::class.java))
            finish()
        }
        binding.addAnother.setOnClickListener{
            binding.fname.text.clear()
            binding.lname.text.clear()
            binding.phone.text.clear()
            binding.email.text.clear()
            binding.nanasaId.text.clear()
            binding.password.text.clear()
            binding.cpassword.text.clear()
            binding.loginLayout.visibility = View.VISIBLE
            binding.loaderLayout.visibility = View.GONE
            binding.successLayout.visibility = View.GONE
        }
    }

    private fun addStudent(){
        var fName = binding.fname.text.toString()
        var lName = binding.lname.text.toString()
        var phone = binding.phone.text.toString()
        var email = binding.email.text.toString()
        var nanasaId = binding.nanasaId.text.toString()
        var password = binding.password.text.toString()
        var cpassword = binding.cpassword.text.toString()
        var teacherPassword = binding.teacherPassword.text.toString()


        if(fName.isNotEmpty() && lName.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && cpassword.isNotEmpty() && nanasaId.isNotEmpty() && teacherPassword.isNotEmpty()) {
            if (password == cpassword) {
                binding.loginLayout.visibility = View.GONE
                binding.loaderLayout.visibility = View.VISIBLE

                var currentEmail = user.currentUser?.email.toString()
                var teacherIndex = user.uid.toString()

                user.signInWithEmailAndPassword(currentEmail,teacherPassword)
                    .addOnCompleteListener{mtask->
                        if(mtask.isSuccessful){

                            user.createUserWithEmailAndPassword(email,password)
                                .addOnCompleteListener{mtask->
                                    if(mtask.isSuccessful){

                                        val uniqueId = user.uid.toString()
                                        val student = Student(fName,lName,email,password,phone,nanasaId,uniqueId,teacherIndex.toString())
                                        FirebaseDatabase.getInstance().getReference("Student/$uniqueId").setValue(student).addOnSuccessListener {

                                            user.signInWithEmailAndPassword(currentEmail,teacherPassword)
                                                .addOnCompleteListener{mtask->
                                                    if(mtask.isSuccessful){
                                                        binding.loginLayout.visibility = View.GONE
                                                        binding.loaderLayout.visibility = View.GONE
                                                        binding.successLayout.visibility = View.VISIBLE
                                                    }else{
                                                        Toast.makeText(
                                                            this,
                                                            "Student Created Successfully. but Server error occured.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        user.signOut()
                                                        startActivity(Intent(this,MainActivity::class.java))
                                                        finish()

                                                    }
                                                }
                                        }.addOnFailureListener{
                                            binding.loginLayout.visibility = View.VISIBLE
                                            binding.loaderLayout.visibility = View.GONE
                                            Toast.makeText(this@AddStudent, "Failed!!. Server Error", Toast.LENGTH_SHORT).show()
                                        }

                                    }else{
                                        Toast.makeText(this, "Failed to create the new student", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        }else{
                            val exception = mtask.exception
                            if (exception is FirebaseAuthInvalidCredentialsException) {
                                val errorCode = exception.errorCode
                                if (errorCode == "ERROR_WRONG_PASSWORD") {
                                    binding.loginLayout.visibility = View.VISIBLE
                                    binding.loaderLayout.visibility = View.GONE
                                    Toast.makeText(this, "Entered Teacher Password is wrong", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Toast.makeText(this, user.uid.toString(), Toast.LENGTH_SHORT).show()

                        }
                    }
            }else{
                Toast.makeText(this, "Passwords are not matched", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Fill all the inputs", Toast.LENGTH_SHORT).show()
        }
    }

}

