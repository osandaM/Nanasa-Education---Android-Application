package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.register.setOnClickListener{
            register()
        }
    }

    private fun register(){
        var fName = binding.fname.text.toString()
        var lName = binding.lname.text.toString()
        var phone = binding.phone.text.toString()
        var email = binding.email.text.toString()
        var password = binding.password.text.toString()
        var cpassword = binding.cpassword.text.toString()

        if(fName.isNotEmpty() && lName.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && cpassword.isNotEmpty()){
            if(password == cpassword){

                binding.loginLayout.visibility = View.GONE
                binding.loaderLayout.visibility = View.VISIBLE

                user.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(MainActivity()){task ->
                        if(task.isSuccessful){

                            val teacher = Teacher(fName,lName,email,phone)
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("Teacher/${user.uid}")
                            usersRef.setValue(teacher)

                            startActivity(Intent(this@Register,Dashboard::class.java))
                            finish()

                        }
                        else{
                            user.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener{mtask->
                                    if(mtask.isSuccessful){
                                        startActivity(Intent(this@Register,Dashboard::class.java))
                                    }else{
                                        binding.loginLayout.visibility = View.VISIBLE
                                        binding.loaderLayout.visibility = View.GONE
                                        Toast.makeText(this@Register,mtask.exception!!.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            Toast.makeText(this@Register, task.exception!!.message, Toast.LENGTH_SHORT).show()
                        }
                    }


            }else{
                Toast.makeText(this, "Passwords are not matched", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Fill all the fields.", Toast.LENGTH_SHORT).show()
        }
    }
}