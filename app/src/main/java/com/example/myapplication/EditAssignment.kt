package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.core.text.set
import androidx.core.view.isNotEmpty
import com.example.myapplication.databinding.ActivityEditAssignmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class EditAssignment : AppCompatActivity() {
    private lateinit var binding:ActivityEditAssignmentBinding
    private lateinit var user:FirebaseAuth

    private val PICK_PDF_FILE = 1
    // Declare and initialize pdfByteArray as an empty ByteArray
    private var pdfByteArray: ByteArray = ByteArray(0)

    private var moduleIndex = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditAssignmentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val assignmentId = intent.getStringExtra("assignmentId")

        readData(assignmentId.toString())

        binding.update.setOnClickListener{
            update(assignmentId.toString(),pdfByteArray)
        }
        binding.uploadPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, PICK_PDF_FILE)
        }

        binding.back.setOnClickListener{
            var intent = Intent(this,ViewAssignment::class.java).also {
                it.putExtra("assignmentId",assignmentId)
                it.putExtra("userType","Teacher")
                it.putExtra("moduleId",moduleIndex)

            }
            startActivity(intent)
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { pdfUri ->
                browsePdfFile(this, pdfUri)
                // Now you can use the pdfByteArray variable to access the contents of the PDF file
            }
        }
    }

    private fun browsePdfFile(context: Context, pdfUri: Uri) {
        context.contentResolver.query(pdfUri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()

            // get the name of the PDF file from the cursor and set it to the textView
            val pdfName = cursor.getString(nameIndex)
            binding.uploadPdfText.text = pdfName.toString()

            // read the PDF file into a ByteArray and assign it to pdfByteArray
            context.contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                pdfByteArray = inputStream.readBytes()
            }
        }
    }

    private fun clear(){
        pdfByteArray = ByteArray(0)
        binding.uploadPdfText.text = "Select the PDF"
    }

    private fun readData(assignmentId:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Assignment").child(assignmentId).get().addOnSuccessListener {
            if(it.exists()){
                var name = it.child("name").value.toString()
                var description = it.child("description").value.toString()
                var instruction = it.child("instruction").value.toString()
                var year = it.child("year").value.toString()
                var month = it.child("month").value.toString()
                var date = it.child("date").value.toString()
                var hour = it.child("hour").value.toString()
                var minute = it.child("minute").value.toString()

                moduleIndex = it.child("moduleId").value.toString()

                binding.name.setText(name)
                binding.description.setText(description)
                binding.instruction.setText(instruction)
                binding.deadlineDate.updateDate(year.toInt(), month.toInt() - 1, date.toInt())
                binding.timePicker.currentHour = hour.toInt()
                binding.timePicker.currentMinute = minute.toInt()


                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE

            }
        }
    }

    private fun update(assignmentId: String,pdfByteArray: ByteArray){

        binding.dataLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.successLayout.visibility = View.GONE


        var name = binding.name.text.toString()
        var description = binding.description.text.toString()
        var instruction = binding.instruction.text.toString()
        var date = binding.deadlineDate
        var time = binding.timePicker
        var uploadText = binding.uploadPdfText.text

        if(name.isNotEmpty() && description.isNotEmpty() && instruction.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty()){

            if(uploadText == "Select the PDF"){
                updateData(assignmentId)
            }else{
                val storageRef = FirebaseStorage.getInstance().reference.child("pdfs/$assignmentId")
                storageRef.delete()
                    .addOnSuccessListener {
                        // File deleted successfully
                        var pdfName = assignmentId
                        val storageReference = FirebaseStorage.getInstance().getReference("pdfs/$pdfName")

                        storageReference.putBytes(pdfByteArray)
                            .addOnSuccessListener {
                                updateData(assignmentId)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Update Failed with errors. Current document got deleted. please add the document again.", Toast.LENGTH_SHORT).show()
                                binding.loaderLayout.visibility = View.GONE
                                binding.dataLayout.visibility = View.VISIBLE
                                binding.successLayout.visibility = View.GONE
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update Failed.", Toast.LENGTH_SHORT).show()
                        binding.loaderLayout.visibility = View.GONE
                        binding.dataLayout.visibility = View.VISIBLE
                        binding.successLayout.visibility = View.GONE
                    }

            }

        }else{
            Toast.makeText(this, "Fill all the details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData(assignmentId:String){
        var name = binding.name.text.toString()
        var description = binding.description.text.toString()
        var instruction = binding.instruction.text.toString()
        var date = binding.deadlineDate
        var time = binding.timePicker

        var year = date.year
        var month = date.month + 1
        var day = date.dayOfMonth

        var hour = time.hour
        var minute = time.minute

        var assignment = AssignmentItem(assignmentId,moduleIndex,name,description,instruction,year.toString(),month.toString(),day.toString(),hour.toString(),minute.toString())

        FirebaseDatabase.getInstance().getReference("Assignment").child(assignmentId).setValue(assignment).addOnSuccessListener {
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.GONE
            binding.successLayout.visibility = View.VISIBLE
        }.addOnFailureListener{
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
            binding.successLayout.visibility = View.GONE
            Toast.makeText(this, "Failed to add the assignment", Toast.LENGTH_SHORT).show()
        }
    }

}