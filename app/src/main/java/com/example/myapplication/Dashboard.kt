package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class Dashboard : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /*
        binding.logout.setOnClickListener{
            user.signOut()
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein,R.anim.so_slide)
            finish()
        }*/

        binding.addStudent.setOnClickListener{
            val intent = Intent(this,AddStudent::class.java)
            startActivity(intent)
        }
        binding.addModule.setOnClickListener{
            val intent = Intent(this,AddModule::class.java)
            startActivity(intent)
        }
        binding.profile.setOnClickListener{
            val intent = Intent(this,Profile::class.java)
            startActivity(intent)
        }
        binding.viewModule.setOnClickListener{
            val intent = Intent(this,ViewModules::class.java)
            startActivity(intent)
        }
        binding.viewStudent.setOnClickListener{
            val intent = Intent(this,ViewStudents::class.java)
            startActivity(intent)
        }

    }
}