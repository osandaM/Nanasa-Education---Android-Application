package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    lateinit var handler: Handler
    private lateinit var binding:ActivityMainBinding
    private lateinit var user:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.mainSplashLogo.alpha = 0f
        binding.mainSplashLogo.animate().setDuration(1000).alpha(1f)

        if (ConnectionCheck.checkForInternet(this)) {
            //device is connected to the internet
            handler = Handler()
            handler.postDelayed({
                if(user.currentUser == null){
                    val intent = Intent(this,Main::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fadein,R.anim.so_slide)
                    finish()
                }else{

                    FirebaseDatabase.getInstance().getReference("Teacher").child(user.uid.toString()).get().addOnSuccessListener {
                        if (it.exists()){
                            val intent = Intent(this,Dashboard::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.fadein,R.anim.so_slide)
                            finish()
                        }else{
                            FirebaseDatabase.getInstance().getReference("Student").child(user.uid.toString()).get().addOnSuccessListener {
                                if(it.exists()){
                                    val intent = Intent(this,StudentDashboard::class.java)
                                    startActivity(intent)
                                    overridePendingTransition(R.anim.fadein,R.anim.so_slide)
                                    finish()
                                }
                            }
                        }
                    }
                }
            },2000)
        } else {
            //device is not connected in to internet
            handler = Handler()
            handler.postDelayed({
                val intent = Intent(this,NoInternet::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.so_slide,R.anim.so_slide)
                finish()
            },2000)
        }
    }
}