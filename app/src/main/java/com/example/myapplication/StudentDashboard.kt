package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityStudentDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentDashboard : AppCompatActivity() {

    private lateinit var binding:ActivityStudentDashboardBinding
    private lateinit var user:FirebaseAuth

    private lateinit var moduleArrayList : ArrayList<Module>
    private lateinit var moduleRecyclerView : RecyclerView

    private var counter = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        moduleRecyclerView = binding.moduleList
        moduleRecyclerView.layoutManager = LinearLayoutManager(this)
        moduleRecyclerView.setHasFixedSize(true)
        moduleArrayList = arrayListOf<Module>()
        readData(user.uid.toString())


        binding.refresh.setOnClickListener{
            readData(user.uid.toString())
        }
        binding.profile.setOnClickListener{
            val intent = Intent(this,Profile::class.java)
            startActivity(intent)
        }
    }

    private fun readData(userid:String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.noDataLayout.visibility = View.GONE
        moduleArrayList.clear()
        counter = 0

        FirebaseDatabase.getInstance().getReference("Enroll")
            .orderByChild("studentId")
            .equalTo(userid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (fineSnapshot in snapshot.children) {
                            if (fineSnapshot.child("studentId").value.toString() == userid) {

                                FirebaseDatabase.getInstance().getReference("Module")
                                    .child(fineSnapshot.child("moduleId").value.toString()).get()
                                    .addOnSuccessListener {
                                        if (it.exists()) {
                                            val moduleItem = it.getValue(Module::class.java)
                                            moduleArrayList.add(moduleItem!!)
                                            counter++
                                            if (counter == snapshot.childrenCount.toInt()) {

                                                // All data has been fetched, set the adapter and hide the loader
                                                moduleRecyclerView.adapter = StudentModuleAdapter(
                                                    moduleArrayList,
                                                    this@StudentDashboard
                                                )
                                                binding.dataLayout.visibility = View.VISIBLE
                                                binding.loaderLayout.visibility = View.GONE
                                            }
                                        }
                                    }
                            }
                        }
                    }else{
                        binding.noDataLayout.visibility = View.VISIBLE
                        binding.loaderLayout.visibility = View.GONE
                    }
                }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }

    fun onItemClick(position: Int) {
        var current = moduleArrayList[position]
        var intent = Intent(this,ModuleItemView::class.java).also {
            it.putExtra("moduleId",current.moduleId)
            it.putExtra("userType","Student")
        }
        startActivity(intent)
    }

}


