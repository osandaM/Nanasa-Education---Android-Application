package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityMain2Binding
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class Main : AppCompatActivity() {
    private lateinit var binding:ActivityMain2Binding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.teacherLogin.setOnClickListener{
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
        }
        binding.studentLogin.setOnClickListener{
            val intent = Intent(this,StudentLogin::class.java)
            startActivity(intent)
        }

    }
}