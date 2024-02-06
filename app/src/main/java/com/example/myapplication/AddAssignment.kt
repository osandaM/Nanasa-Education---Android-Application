package com.example.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityAddAssignmentBinding
import com.google.firebase.auth.FirebaseAuth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isNotEmpty
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream
import java.util.UUID


class AddAssignment : AppCompatActivity() {

    private lateinit var binding: ActivityAddAssignmentBinding
    private lateinit var user:FirebaseAuth

    private val PICK_PDF_FILE = 1
    // Declare and initialize pdfByteArray as an empty ByteArray
    private var pdfByteArray: ByteArray = ByteArray(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddAssignmentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val moduleId = intent.getStringExtra("moduleId")

        readData(moduleId.toString())

        binding.upload.setOnClickListener{
            uploadPdfToFirebaseStorage(pdfByteArray,moduleId.toString())
        }
        binding.uploadPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, PICK_PDF_FILE)
        }

        binding.clear.setOnClickListener{
            clear()
        }
        binding.backToDash.setOnClickListener{
            var intent = Intent(this,ModuleItemView::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
                it.putExtra("userType","Teacher")
            }
            startActivity(intent)
            finish()
        }
        binding.addNewAssignment.setOnClickListener{
            var intent = Intent(this,ModuleItemView::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
                it.putExtra("userType","Teacher")
            }
            startActivity(intent)
            finish()
        }


    }

    private fun readData(moduleId: String){
        binding.successLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
            if(it.exists()){
                binding.moduleName.text = "Module Name: ${it.child("name").value.toString()}"
                binding.loaderLayout.visibility = View.GONE
                binding.successLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }
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

    private fun uploadPdfToFirebaseStorage(pdfByteArray: ByteArray, moduleId: String) {

        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        binding.successLayout.visibility = View.GONE

        var name = binding.name.text.toString()
        var description = binding.description.text.toString()
        var instruction = binding.instruction.text.toString()
        var date = binding.deadlineDate
        var time = binding.timePicker

        if(name.isNotEmpty() && description.isNotEmpty() && instruction.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && binding.uploadPdfText.text != "Select the PDF"){
            var pdfName = UUID.randomUUID()
            val storageReference = FirebaseStorage.getInstance().getReference("pdfs/$pdfName")

            storageReference.putBytes(pdfByteArray)
                .addOnSuccessListener {
                    upload(moduleId,pdfName.toString())
                }
                .addOnFailureListener {
                    binding.loaderLayout.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                    binding.successLayout.visibility = View.GONE
                }
        }else{
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
            binding.successLayout.visibility = View.GONE
            Toast.makeText(this, "Fill all the inputs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun upload(moduleId:String,assignmentId:String){

        var name = binding.name.text.toString()
        var description = binding.description.text.toString()
        var instruction = binding.instruction.text.toString()
        var date = binding.deadlineDate
        var time = binding.timePicker

        if(name.isNotEmpty() && description.isNotEmpty() && instruction.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && binding.uploadPdfText.text != "Select the PDF"){
            var year = date.year
            var month = date.month + 1
            var date = date.dayOfMonth

            var hour = time.hour
            var minute = time.minute

            var assignment = AssignmentItem(assignmentId,moduleId,name,description,instruction,year.toString(),month.toString(),date.toString(),hour.toString(),minute.toString())

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

        }else{
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
            binding.successLayout.visibility = View.GONE
            Toast.makeText(this, "Fill all the inputs", Toast.LENGTH_SHORT).show()
        }
    }

}