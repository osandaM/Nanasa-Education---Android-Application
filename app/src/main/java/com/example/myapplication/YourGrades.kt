package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityYourGradesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourGrades : AppCompatActivity() {
    private lateinit var binding: ActivityYourGradesBinding
    private lateinit var user:FirebaseAuth

    private lateinit var gradeList : ArrayList<Grade>
    private lateinit var gradeRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityYourGradesBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")

        gradeRecyclerView = binding.gradelist
        gradeRecyclerView.layoutManager = LinearLayoutManager(this)
        gradeRecyclerView.setHasFixedSize(true)
        gradeList = arrayListOf<Grade>()
        readData(moduleId.toString())
    }

    private fun readData(moduleId:String){
        var studentId = user.uid.toString()
        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.noDataLayout.visibility = View.GONE
        gradeList.clear()
        FirebaseDatabase.getInstance().getReference("Grade")
            .orderByChild("moduleId")
            .equalTo(moduleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(fineSnapshot in snapshot.children){
                            if(fineSnapshot.child("studentId").value.toString() == studentId){
                                val gradeItem =  fineSnapshot.getValue(Grade::class.java)
                                gradeList.add(gradeItem!!)
                            }
                        }
                        gradeRecyclerView.adapter = YourGradeAdapter(gradeList,this@YourGrades)
                        binding.dataLayout.visibility = View.VISIBLE
                        binding.loaderLayout.visibility = View.GONE
                        cal()
                    }else{
                        binding.dataLayout.visibility = View.GONE
                        binding.loaderLayout.visibility = View.GONE
                        binding.noDataLayout.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@YourGrades, "error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cal(){
        for(grade in gradeList){
            var marks = grade.marks?.toInt()
            var outOf = grade.outOff?.toInt()

            var x = 100 / outOf!!
            var fmarks = marks!! * x
            var total = 0
            total += fmarks

            var average = total / gradeList.size
            binding.totalText.text = "Your Average : $average/100"
        }
    }

    fun onItemClick(position: Int) {
        var current = gradeList[position]
        var intent = Intent(this,ModuleItemView::class.java).also {
            it.putExtra("moduleId",current.moduleId)
            it.putExtra("userType","Teacher")
        }
        startActivity(intent)
    }
}