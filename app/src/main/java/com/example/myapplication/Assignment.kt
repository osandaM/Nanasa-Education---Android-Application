package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityAssignmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Assignment : AppCompatActivity() {

    private lateinit var binding:ActivityAssignmentBinding
    private lateinit var user: FirebaseAuth

    private lateinit var assignmentArrayList : ArrayList<AssignmentItem>
    private lateinit var assignmentRecyclerView : RecyclerView

    private var usertype = ""
    private var moduleIndex = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAssignmentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")
        val userType = intent.getStringExtra("userType")

        moduleIndex = moduleId.toString()

        usertype = userType.toString()

        assignmentRecyclerView = binding.assignmentList
        assignmentRecyclerView.layoutManager = LinearLayoutManager(this)
        assignmentRecyclerView.setHasFixedSize(true)
        assignmentArrayList = arrayListOf<AssignmentItem>()
        readData(moduleId.toString())


        binding.addAssignment.setOnClickListener{
            var intent = Intent(this,AddAssignment::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
        }

        if(userType == "Teacher"){
            binding.addAssignmentBar.visibility = View.VISIBLE
        }else{
            binding.addAssignmentBar.visibility = View.GONE
        }
    }

    private fun readData(moduleId:String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.noDataLayout.visibility = View.GONE
        assignmentArrayList.clear()
        FirebaseDatabase.getInstance().getReference("Assignment")
            .orderByChild("moduleId")
            .equalTo(moduleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(fineSnapshot in snapshot.children){
                            val assignment =  fineSnapshot.getValue(AssignmentItem::class.java)
                            assignmentArrayList.add(assignment!!)
                        }
                        assignmentRecyclerView.adapter = AssignmentAdapter(assignmentArrayList,this@Assignment)
                        binding.dataLayout.visibility = View.VISIBLE
                        binding.loaderLayout.visibility = View.GONE
                        binding.noDataLayout.visibility = View.GONE
                    }else{
                        binding.dataLayout.visibility = View.GONE
                        binding.loaderLayout.visibility = View.GONE
                        binding.noDataLayout.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Assignment, "error", Toast.LENGTH_SHORT).show()
                }
            })
    }
    fun onItemClick(position: Int) {
        var current = assignmentArrayList[position]
        var intent = Intent(this,ViewAssignment::class.java).also {
            it.putExtra("assignmentId",current.assignmentId)
            it.putExtra("userType",usertype)
            it.putExtra("moduleId",moduleIndex)
        }
        startActivity(intent)
    }

}