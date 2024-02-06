package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityStudentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase

class StudentLogin : AppCompatActivity() {
    private lateinit var binding:ActivityStudentLoginBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStudentLoginBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.login.setOnClickListener{
            login()
        }

    }

    private fun login(){
        var email = binding.email.text.toString()
        var password = binding.password.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            binding.loginLayout.visibility = View.GONE
            binding.loaderLayout.visibility = View.VISIBLE
            //user login only
            user.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{mtask->
                    if(mtask.isSuccessful){

                        FirebaseDatabase.getInstance().getReference("Student").child(user.uid.toString()).get().addOnSuccessListener {
                            if(it.exists()){
                                val intent = Intent(this,StudentDashboard::class.java)
                                startActivity(intent)
                                overridePendingTransition(R.anim.fadein,R.anim.so_slide)
                                finish()
                            }else{
                                user.signOut()
                                binding.loginLayout.visibility = View.VISIBLE
                                binding.loaderLayout.visibility = View.GONE
                                Toast.makeText(this, "Entered Details are wrong.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        val exception = mtask.exception
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            val errorCode = exception.errorCode
                            if (errorCode == "ERROR_WRONG_PASSWORD") {
                                binding.loginLayout.visibility = View.VISIBLE
                                binding.loaderLayout.visibility = View.GONE
                                Toast.makeText(this, "Entered Password is wrong", Toast.LENGTH_SHORT).show()
                            } else if (errorCode == "ERROR_INVALID_EMAIL") {
                                binding.loginLayout.visibility = View.VISIBLE
                                binding.loaderLayout.visibility = View.GONE
                                Toast.makeText(this, "Entered Email Address is wrong", Toast.LENGTH_SHORT).show()
                            } else {
                                binding.loginLayout.visibility = View.VISIBLE
                                binding.loaderLayout.visibility = View.GONE
                                Toast.makeText(this, "Server Error", Toast.LENGTH_SHORT).show()
                            }
                        } else if (exception is FirebaseAuthInvalidUserException) {
                            binding.loginLayout.visibility = View.VISIBLE
                            binding.loaderLayout.visibility = View.GONE
                            val errorCode = exception.errorCode
                            Toast.makeText(this, errorCode, Toast.LENGTH_SHORT).show()
                        } else {
                            binding.loginLayout.visibility = View.VISIBLE
                            binding.loaderLayout.visibility = View.GONE
                            Toast.makeText(this, "Server Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }else{
            //password email empty
            binding.loginLayout.visibility = View.VISIBLE
            binding.loaderLayout.visibility = View.GONE
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
}