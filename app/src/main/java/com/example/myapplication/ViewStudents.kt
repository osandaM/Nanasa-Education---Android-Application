package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityViewStudentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ViewStudents : AppCompatActivity() {
    private lateinit var binding:ActivityViewStudentsBinding
    private lateinit var user:FirebaseAuth
    private lateinit var studentArrayList : ArrayList<Student>
    private lateinit var studentRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewStudentsBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        studentRecyclerView = binding.studentList
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.setHasFixedSize(true)
        studentArrayList = arrayListOf<Student>()
        readData(user.uid.toString())

    }

    private fun readData(userid:String){
        binding.dataLayout.visibility = View.GONE
        binding.noDataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        studentArrayList.clear()
        FirebaseDatabase.getInstance().getReference("Student")
            .orderByChild("teacherId")
            .equalTo(userid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(fineSnapshot in snapshot.children){
                        val studentItem =  fineSnapshot.getValue(Student::class.java)
                        studentArrayList.add(studentItem!!)
                    }
                    studentRecyclerView.adapter = StudentAdapter(studentArrayList,this@ViewStudents)
                    binding.dataLayout.visibility = View.VISIBLE
                    binding.loaderLayout.visibility = View.GONE
                }else{
                    binding.dataLayout.visibility = View.GONE
                    binding.noDataLayout.visibility = View.VISIBLE
                    binding.loaderLayout.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewStudents, "error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun onItemClick(position: Int) {
        var current = studentArrayList[position]
        var intent = Intent(this,ViewStudent::class.java).also {
            it.putExtra("studentId",current.studentId)
        }
        startActivity(intent)
        finish()
    }

}