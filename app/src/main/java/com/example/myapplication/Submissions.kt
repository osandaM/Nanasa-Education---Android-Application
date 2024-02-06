package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivitySubmissionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Submissions : AppCompatActivity() {

    private lateinit var binding:ActivitySubmissionsBinding
    private lateinit var user:FirebaseAuth

    private lateinit var subArrayList : ArrayList<Submit>
    private lateinit var subRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySubmissionsBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val assignmentId = intent.getStringExtra("assignmentId")
        val moduleId = intent.getStringExtra("moduleId")

        subRecyclerView = binding.subsList
        subRecyclerView.layoutManager = LinearLayoutManager(this)
        subRecyclerView.setHasFixedSize(true)
        subArrayList = arrayListOf<Submit>()
        readData(assignmentId.toString(),moduleId.toString())

        binding.notMarked.setOnClickListener{
            readData(assignmentId.toString(),moduleId.toString())
        }
        binding.marked.setOnClickListener{
            readDataMarked(assignmentId.toString(),moduleId.toString())
        }

    }

    private fun readData(assignmentId:String,moduleId:String){
        binding.notMarked.setBackground(ContextCompat.getDrawable(this, R.drawable.gray_btn));
        binding.marked.setBackground(ContextCompat.getDrawable(this, R.drawable.main_btn));
        binding.marked.setTextColor(getColor(R.color.white));
        binding.notMarked.setTextColor(getColor(R.color.black));

        subArrayList.clear()

        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Assignment").child(assignmentId).get().addOnSuccessListener {
            if(it.exists()){
                binding.assignmentName.text = it.child("name").value.toString()

                FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
                    if(it.exists()){
                        binding.moduleName.text = it.child("name").value.toString()



                        FirebaseDatabase.getInstance().getReference("Submit")
                            .orderByChild("assignmentId")
                            .equalTo(assignmentId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        for(fineSnapshot in snapshot.children){
                                            if(fineSnapshot.child("status").value.toString() == "pending"){
                                                val subItem =  fineSnapshot.getValue(Submit::class.java)
                                                subArrayList.add(subItem!!)
                                            }
                                        }
                                        subRecyclerView.adapter = SubmitAdapter(subArrayList,this@Submissions)
                                        binding.dataLayout.visibility = View.VISIBLE
                                        binding.loaderLayout.visibility = View.GONE
                                    }else{
                                        binding.dataLayout.visibility = View.GONE
                                        binding.loaderLayout.visibility = View.GONE
                                        binding.noDataLayout.visibility = View.VISIBLE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Submissions, "error", Toast.LENGTH_SHORT).show()
                                }
                            })



                    }
                }
            }
        }
    }

    private fun readDataMarked(assignmentId:String,moduleId:String){
        binding.notMarked.setBackground(ContextCompat.getDrawable(this, R.drawable.main_btn));
        binding.marked.setBackground(ContextCompat.getDrawable(this, R.drawable.gray_btn));
        binding.marked.setTextColor(getColor(R.color.black));
        binding.notMarked.setTextColor(getColor(R.color.white));
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        subArrayList.clear()
        FirebaseDatabase.getInstance().getReference("Assignment").child(assignmentId).get().addOnSuccessListener {
            if(it.exists()){
                binding.assignmentName.text = it.child("name").value.toString()

                FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
                    if(it.exists()){
                        binding.moduleName.text = it.child("name").value.toString()
                        FirebaseDatabase.getInstance().getReference("Submit")
                            .orderByChild("assignmentId")
                            .equalTo(assignmentId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        for(fineSnapshot in snapshot.children){
                                            if(fineSnapshot.child("status").value.toString() == "done"){
                                                val subItem =  fineSnapshot.getValue(Submit::class.java)
                                                subArrayList.add(subItem!!)
                                            }
                                        }
                                        subRecyclerView.adapter = SubmitAdapter(subArrayList,this@Submissions)
                                        binding.dataLayout.visibility = View.VISIBLE
                                        binding.loaderLayout.visibility = View.GONE
                                    }else{
                                        binding.dataLayout.visibility = View.GONE
                                        binding.loaderLayout.visibility = View.GONE
                                        binding.noDataLayout.visibility = View.VISIBLE
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@Submissions, "error", Toast.LENGTH_SHORT).show()
                                }
                            })



                    }
                }
            }
        }
    }

    fun onItemClick(position: Int) {
        var current = subArrayList[position]
        var intent = Intent(this,ViewSubmission::class.java).also {
            it.putExtra("submitId",current.submitId)
            it.putExtra("status",current.status)
            it.putExtra("assignmentId",current.assignmentId)
            it.putExtra("moduleId",current.moduleId)
        }
        startActivity(intent)
        finish()
    }
}