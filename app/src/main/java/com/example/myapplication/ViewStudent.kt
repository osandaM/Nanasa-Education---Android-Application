package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityViewStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewStudent : AppCompatActivity() {
    private lateinit var binding:ActivityViewStudentBinding
    private lateinit var user:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewStudentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val studentId = intent.getStringExtra("studentId")

        readData(studentId.toString())

        binding.delete.setOnClickListener{
            binding.deleteConfirm.visibility = View.VISIBLE
        }
        binding.cancelDelete.setOnClickListener{
            binding.deleteConfirm.visibility = View.GONE
        }
        binding.ConfirmDelete.setOnClickListener{
            delete(studentId.toString())
            //Toast.makeText(this, "eeee", Toast.LENGTH_SHORT).show()
        }
        binding.edit.setOnClickListener{
            binding.viewStudentLayout.visibility = View.GONE
            binding.editStudentLayout.visibility = View.VISIBLE
            editReadData(studentId.toString())
        }
        binding.editFromBack.setOnClickListener{
            binding.viewStudentLayout.visibility = View.VISIBLE
            binding.editStudentLayout.visibility = View.GONE
        }
        binding.update.setOnClickListener{
            update(studentId.toString())
        }
    }
    private fun readData(studentId:String){

        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        binding.viewStudentLayout.visibility = View.GONE

        FirebaseDatabase.getInstance().getReference("Student").child(studentId).get().addOnSuccessListener { 
            if(it.exists()){
                var firstName = it.child("firstName").value.toString()
                var lastName = it.child("lastName").value.toString()
                var email = it.child("email").value.toString()
                var nanasaId = it.child("nanasaId").value.toString()
                var phone = it.child("phone").value.toString()

                binding.name.text = "$firstName $lastName"
                binding.email.text = email
                binding.nanasaId.text = nanasaId
                binding.phone.text = phone

                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
                binding.viewStudentLayout.visibility = View.VISIBLE

            }else{
                Toast.makeText(this, "Student not found! Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun delete(studentId: String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE

        FirebaseDatabase.getInstance().getReference("Student").child(studentId).removeValue().addOnSuccessListener {
            FirebaseDatabase.getInstance().getReference("Enroll")
                .orderByChild("studentId")
                .equalTo(studentId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(fineSnapshot in snapshot.children){
                                FirebaseDatabase.getInstance().getReference("Enroll").child(fineSnapshot.key.toString()).removeValue().addOnSuccessListener {  }
                            }
                            Toast.makeText(this@ViewStudent, "Student Deleted successfully", Toast.LENGTH_SHORT).show()
                            var intent = Intent(this@ViewStudent,ViewStudents::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this@ViewStudent, "Student Deleted successfully", Toast.LENGTH_SHORT).show()
                            var intent = Intent(this@ViewStudent,ViewStudents::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ViewStudent, "Deleted the student with errors.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    private fun editReadData(studentId: String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Student").child(studentId).get().addOnSuccessListener {
            if(it.exists()){
                var firstName = it.child("firstName").value.toString()
                var lastName = it.child("lastName").value.toString()
                var phone = it.child("phone").value.toString()

                binding.fNameEdit.setText(firstName)
                binding.lNameEdit.setText(lastName)
                binding.phoneEdit.setText(phone)

                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE

            }else{
                Toast.makeText(this, "Student not found! Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun update(studentId: String){

        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE

        var fname = binding.fNameEdit.text.toString()
        var lname = binding.lNameEdit.text.toString()
        var phone = binding.phoneEdit.text.toString()

        if(fname.isNotEmpty() && lname.isNotEmpty() && phone.isNotEmpty()){
            FirebaseDatabase.getInstance().getReference("Student").child("$studentId/firstName").setValue(fname).addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Student").child("$studentId/lastName").setValue(lname).addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("Student").child("$studentId/phone").setValue(phone).addOnSuccessListener {
                        Toast.makeText(this, "Student Updated successfully", Toast.LENGTH_SHORT).show()
                        binding.loaderLayout.visibility = View.GONE
                        binding.dataLayout.visibility = View.VISIBLE
                        binding.viewStudentLayout.visibility = View.VISIBLE
                        binding.editStudentLayout.visibility = View.GONE
                        readData(studentId)
                    }
                }
            }
        }
    }
}