package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityViewModulesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewModules : AppCompatActivity() {
    private lateinit var binding:ActivityViewModulesBinding
    private lateinit var user:FirebaseAuth
    private lateinit var moduleArrayList : ArrayList<Module>
    private lateinit var moduleRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewModulesBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        moduleRecyclerView = binding.moduleList
        moduleRecyclerView.layoutManager = LinearLayoutManager(this)
        moduleRecyclerView.setHasFixedSize(true)
        moduleArrayList = arrayListOf<Module>()
        readData(user.uid.toString())
    }

    private fun readData(userid:String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.noDataLayout.visibility = View.GONE
        moduleArrayList.clear()
        FirebaseDatabase.getInstance().getReference("Module")
            .orderByChild("teacherId")
            .equalTo(userid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(fineSnapshot in snapshot.children){
                        val moduleItem =  fineSnapshot.getValue(Module::class.java)
                        moduleArrayList.add(moduleItem!!)
                    }
                    moduleRecyclerView.adapter = ModuleAdapter(moduleArrayList,this@ViewModules)
                    binding.dataLayout.visibility = View.VISIBLE
                    binding.loaderLayout.visibility = View.GONE
                }else{
                    binding.dataLayout.visibility = View.GONE
                    binding.loaderLayout.visibility = View.GONE
                    binding.noDataLayout.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewModules, "error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun onItemClick(position: Int) {
        var current = moduleArrayList[position]
        var intent = Intent(this,ModuleItemView::class.java).also {
            it.putExtra("moduleId",current.moduleId)
            it.putExtra("userType","Teacher")
        }
        startActivity(intent)
    }
}