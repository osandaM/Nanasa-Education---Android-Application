package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityAddModuleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AddModule : AppCompatActivity() {
    private lateinit var binding:ActivityAddModuleBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddModuleBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.addModule.setOnClickListener{
            addModule()
        }

        binding.dashboard.setOnClickListener{
            startActivity(Intent(this,Dashboard::class.java))
            finish()
        }
        binding.addAnother.setOnClickListener{
            binding.moduleName.text.clear()
            binding.code.text.clear()
            binding.description.text.clear()
            binding.loginLayout.visibility = View.VISIBLE
            binding.loaderLayout.visibility = View.GONE
            binding.successLayout.visibility = View.GONE
        }
    }

    private fun addModule(){
        var moduleName = binding.moduleName.text.toString()
        var code = binding.code.text.toString()
        var description = binding.description.text.toString()

        var moduleId = UUID.randomUUID()

        if(moduleName.isNotEmpty() && code.isNotEmpty() && description.isNotEmpty()){
            binding.loginLayout.visibility = View.GONE
            binding.loaderLayout.visibility = View.VISIBLE
            var module = Module(moduleName,code,description,moduleId.toString(),user.uid.toString())
            FirebaseDatabase.getInstance().getReference("Module").child(moduleId.toString()).setValue(module).addOnSuccessListener {
                binding.loginLayout.visibility = View.GONE
                binding.loaderLayout.visibility = View.GONE
                binding.successLayout.visibility = View.VISIBLE
            }
        }else{
            Toast.makeText(this, "Fill all inputs", Toast.LENGTH_SHORT).show()
            binding.loginLayout.visibility = View.VISIBLE
            binding.loaderLayout.visibility = View.GONE
        }
    }
}