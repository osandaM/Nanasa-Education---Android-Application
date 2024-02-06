package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityModuleItemViewBinding
import com.example.myapplication.databinding.ActivityViewModuleStudentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewModuleStudents : AppCompatActivity() {
    private lateinit var binding: ActivityViewModuleStudentsBinding
    private lateinit var user:FirebaseAuth
    private lateinit var studentArrayList : ArrayList<Student>
    private lateinit var studentRecyclerView : RecyclerView

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewModuleStudentsBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")

        studentRecyclerView = binding.studentList
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.setHasFixedSize(true)
        studentArrayList = arrayListOf<Student>()
        readData(moduleId.toString())

        binding.addStudent.setOnClickListener{
            var intent = Intent(this,AddModuleStudents::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
            finish()
        }

    }

    private fun readData(moduleId:String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        studentArrayList.clear()
        counter = 0

        FirebaseDatabase.getInstance().getReference("Enroll")
            .orderByChild("moduleId")
            .equalTo(moduleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){

                    for(childSnapshot in snapshot.children){
                        var studentId = childSnapshot.child("studentId").value.toString()
                        FirebaseDatabase.getInstance().getReference("Student").child(studentId).get().addOnSuccessListener {
                            if (it.exists()){
                                //Toast.makeText(this@ViewModuleStudents, it.value.toString(), Toast.LENGTH_SHORT).show()

                                val studentItem =  it.getValue(Student::class.java)
                                studentArrayList.add(studentItem!!)
                                counter++

                                if (counter == snapshot.childrenCount.toInt()) {
                                    // All data has been fetched, set the adapter and hide the loader
                                    studentRecyclerView.adapter = ModuleStudentAdapter(studentArrayList,this@ViewModuleStudents)
                                    binding.dataLayout.visibility = View.VISIBLE
                                    binding.loaderLayout.visibility = View.GONE
                                }
                            }
                        }
                    }

                }else{
                    //no data
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewModuleStudents, "error", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun onItemClick(position: Int) {

    }
}