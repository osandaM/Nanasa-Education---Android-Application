package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityAddModuleStudentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class AddModuleStudents : AppCompatActivity() {
    private lateinit var binding:ActivityAddModuleStudentsBinding
    private lateinit var user:FirebaseAuth
    private lateinit var studentArrayList : ArrayList<Student>
    private lateinit var studentRecyclerView : RecyclerView

    private var moduleIndex = ""

    private var counter = 0
    private var childCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddModuleStudentsBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")
        moduleIndex = moduleId.toString()

        studentRecyclerView = binding.studentList
        studentRecyclerView.layoutManager = LinearLayoutManager(this)
        studentRecyclerView.setHasFixedSize(true)
        studentArrayList = arrayListOf<Student>()
        readData(user.uid.toString(),moduleId.toString())

    }

    private fun readData(userid:String,moduleId: String){
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        studentArrayList.clear()
        counter = 0

        FirebaseDatabase.getInstance().getReference("Student")
            .orderByChild("teacherId")
            .equalTo(userid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(childSnapshot in snapshot.children){

                        FirebaseDatabase.getInstance().getReference("Enroll")
                            .orderByChild("studentId")
                            .equalTo(childSnapshot.key.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(enrollSnap: DataSnapshot) {
                                    counter++
                                    if(enrollSnap.exists()){
                                        childCounter = 0
                                        for(childEnroll in enrollSnap.children){
                                            childCounter++
                                            if(childEnroll.child("moduleId").value.toString() == moduleId){
                                                break
                                            }

                                            if(childCounter == enrollSnap.childrenCount.toInt()){
                                                val studentItem =  childSnapshot.getValue(Student::class.java)
                                                studentArrayList.add(studentItem!!)
                                            }

                                        }
                                    }else{
                                        //can add user
                                        val studentItem =  childSnapshot.getValue(Student::class.java)
                                        studentArrayList.add(studentItem!!)
                                    }

                                    if(counter == snapshot.childrenCount.toInt()){
                                        studentRecyclerView.adapter = AddStudentAdapter(studentArrayList,this@AddModuleStudents)
                                        binding.dataLayout.visibility = View.VISIBLE
                                        binding.loaderLayout.visibility = View.GONE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@AddModuleStudents, "Error retrieving Enroll data", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddModuleStudents, "error2", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun onItemClick(position: Int) {
        var intent = Intent(this@AddModuleStudents,ConfirmAddModuleStudent::class.java).also {
            it.putExtra("studentId",studentArrayList[position].studentId)
            it.putExtra("moduleId",moduleIndex)
        }
        startActivity(intent)
        finish()
    }
}