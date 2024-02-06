package com.example.myapplication

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityViewAssignmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit


class ViewAssignment : AppCompatActivity() {
    private lateinit var binding:ActivityViewAssignmentBinding
    private lateinit var user:FirebaseAuth

    private val PICK_PDF_FILE = 1
    // Declare and initialize pdfByteArray as an empty ByteArray
    private var pdfByteArray: ByteArray = ByteArray(0)

    private var submitIndex = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewAssignmentBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val assignmentId = intent.getStringExtra("assignmentId")
        val userType = intent.getStringExtra("userType")
        val moduleId = intent.getStringExtra("moduleId")
        readData(assignmentId.toString(),moduleId.toString(),userType.toString())

        if(userType == "Teacher"){
            binding.teacher.visibility = View.VISIBLE
            binding.student.visibility = View.GONE
        }else{
            binding.teacher.visibility = View.GONE
            binding.student.visibility = View.VISIBLE
        }

        binding.download.setOnClickListener{
            download(assignmentId.toString())
        }

        binding.edit.setOnClickListener{
            var intent = Intent(this,EditAssignment::class.java).also {
                it.putExtra("assignmentId",assignmentId)
            }
            startActivity(intent)
        }

        binding.delete.setOnClickListener{
            binding.deleteAssignment.visibility = View.VISIBLE
        }
        binding.deleteConfirm.setOnClickListener{
            delete(assignmentId.toString(),moduleId.toString())
        }
        binding.deleteCancel.setOnClickListener{
            binding.deleteAssignment.visibility = View.GONE
        }

        binding.upload.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, PICK_PDF_FILE)
        }

        binding.clear.setOnClickListener{
            clear()
        }
        
        binding.submit.setOnClickListener{
            submit(assignmentId.toString(),moduleId.toString())
        }
        binding.refresh.setOnClickListener{
            readData(assignmentId.toString(),moduleId.toString(),userType.toString())
        }
        binding.backToModules.setOnClickListener{
            var intent = Intent(this,ModuleItemView::class.java).also {
                it.putExtra("moduleId",moduleId.toString())
                it.putExtra("userType",userType.toString())
            }
            startActivity(intent)
            finish()
        }
        binding.deleteSub.setOnClickListener{
            deleteSub(submitIndex)
        }
        binding.deleteSubConfirm.setOnClickListener{
            deleteSubConfirm(submitIndex,assignmentId.toString(),moduleId.toString(),userType.toString())
        }
        binding.deleteSubCancel.setOnClickListener{
            binding.deleteSubConfirm.visibility = View.GONE
            binding.deleteSubCancel.visibility = View.GONE
        }
        binding.viewSubs.setOnClickListener{
            var intent = Intent(this,Submissions::class.java).also {
                it.putExtra("assignmentId",assignmentId.toString())
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
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
        binding.uploadPdfText.text = "Upload your document"
    }

    private fun readData(assignmentId:String,moduleId:String,userType:String){

        binding.dataLayout.visibility = View.GONE
        binding.successLayout.visibility = View.GONE
        binding.loaderLayout.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().getReference("Assignment").child(assignmentId).get().addOnSuccessListener {
            if(it.exists()){
                binding.assignmentName.text = it.child("name").value.toString()

                var year = it.child("year").value.toString()
                var month = it.child("month").value.toString()
                var date = it.child("date").value.toString()
                var hour = it.child("hour").value.toString()
                var minute = it.child("minute").value.toString()
                var remainingTime = getRemainingTime(year.toInt(),month.toInt(),date.toInt(),hour.toInt(),minute.toInt())

                binding.remaining.text = remainingTime
                binding.assignmentName2.text = it.child("name").value.toString()
                binding.description.text = it.child("description").value.toString()
                binding.instruction.text = it.child("instruction").value.toString()

                FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
                    if(it.exists()){
                        binding.moduleName.text = it.child("name").value.toString()


                        //checking the user uploaded or not
                        FirebaseDatabase.getInstance().getReference("Submit")
                            .orderByChild("studentId")
                            .equalTo(user.uid.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        var isSubmitted = false
                                        for(fineSnapshot in snapshot.children){
                                            if(fineSnapshot.child("assignmentId").value.toString() == assignmentId){
                                                submitIndex = fineSnapshot.key.toString()
                                                binding.student.visibility = View.GONE
                                                binding.submitted.visibility = View.VISIBLE
                                                isSubmitted = true
                                                break
                                            }
                                        }
                                        if(!isSubmitted && remainingTime == "Assignment Closed"){
                                            binding.student.visibility = View.GONE
                                            binding.submitted.visibility = View.GONE
                                            binding.closedSub.visibility = View.VISIBLE
                                            Toast.makeText(this@ViewAssignment, "tue", Toast.LENGTH_SHORT).show()
                                        }else if(remainingTime == "Assignment Closed" && isSubmitted){
                                            binding.deleteSub.visibility = View.GONE
                                        }
                                        binding.dataLayout.visibility = View.VISIBLE
                                        binding.loaderLayout.visibility = View.GONE
                                    }else{
                                        if(remainingTime == "Assignment Closed"){
                                            binding.student.visibility = View.GONE
                                            binding.submitted.visibility = View.GONE
                                            binding.closedSub.visibility = View.VISIBLE
                                            binding.dataLayout.visibility = View.VISIBLE
                                            binding.loaderLayout.visibility = View.GONE
                                        }else{
                                            binding.dataLayout.visibility = View.VISIBLE
                                            binding.loaderLayout.visibility = View.GONE
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ViewAssignment, "error", Toast.LENGTH_SHORT).show()
                                }
                            })

                    }
                }

            }
        }
    }

    private fun getRemainingTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, 0)
        val targetTimeInMillis = calendar.timeInMillis
        val currentTimeInMillis = System.currentTimeMillis()
        val remainingTimeInMillis = targetTimeInMillis - currentTimeInMillis

        val days = TimeUnit.MILLISECONDS.toDays(remainingTimeInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingTimeInMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeInMillis) % 60

        if(minutes < 0 || hours < 0 || days < 0 ){
            return "Assignment Closed"
        }else{
            return "$days days $hours hours $minutes minutes"
        }
    }

    private fun download(assignmentId: String){
        val storageRef = FirebaseStorage.getInstance().reference.child("pdfs/$assignmentId")
        val localDir = File(getExternalFilesDir(null), "MyPDFs")
        if (!localDir.exists()) {
            localDir.mkdir()
        }
        val localFile = File(localDir, "file.pdf")

        storageRef.getFile(localFile).addOnSuccessListener {
            // File downloaded successfully
            val notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("PDF Download")
                .setContentText("PDF file downloaded successfully")
                .setSmallIcon(R.drawable.assignment)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Check if the device supports notification channels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("CHANNEL_ID", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(1, notificationBuilder.build())

            // Open the downloaded file
            val intent = Intent(Intent.ACTION_VIEW)
            val fileUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", localFile)
            intent.setDataAndType(fileUri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }.addOnFailureListener {
            // File download failed
            Toast.makeText(this, "Failed to download", Toast.LENGTH_SHORT).show()
        }
    }

    private fun delete(assignmentId: String,moduleId: String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        val storageRef = FirebaseStorage.getInstance().reference.child("pdfs/$assignmentId")
        storageRef.delete()
            .addOnSuccessListener {
                // File deleted successfully
                val database = FirebaseDatabase.getInstance()
                val collectionRef = database.getReference("Assignment").child(assignmentId)
                collectionRef.removeValue()
                    .addOnSuccessListener {
                        var intent = Intent(this,Assignment::class.java).also {
                            it.putExtra("moduleId",moduleId)
                        }
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed. (Document deleted. details are not deleted)", Toast.LENGTH_SHORT).show()
                        binding.loaderLayout.visibility = View.GONE
                        binding.dataLayout.visibility = View.VISIBLE
                    }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete the assignment", Toast.LENGTH_SHORT).show()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun submit(assignmentId: String,moduleId: String){

        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        binding.successLayout.visibility = View.GONE

        var uploadPdfText = binding.uploadPdfText.text
        if(uploadPdfText.toString() != "Upload your document"){

            var pdfName = UUID.randomUUID()
            val storageReference = FirebaseStorage.getInstance().getReference("submit/$pdfName")

            storageReference.putBytes(pdfByteArray)
                .addOnSuccessListener {
                    upload(moduleId,assignmentId,pdfName.toString())
                }
                .addOnFailureListener {
                    binding.loaderLayout.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                    binding.successLayout.visibility = View.GONE
                }

        }else{
            Toast.makeText(this, "Upload your file to submit", Toast.LENGTH_SHORT).show()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun upload(moduleId: String, assignmentId: String, submitId:String){
        var uploadPdfText = binding.uploadPdfText.text
        
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()
        
        if(uploadPdfText.toString() != "Upload your document"){

            FirebaseDatabase.getInstance().getReference("Student").child(user.uid.toString()).get().addOnSuccessListener {
                if(it.exists()){
                    var fName = it.child("firstName").value.toString()
                    var lName = it.child("lastName").value.toString()

                    FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
                        if(it.exists()){
                            var assignmentName = binding.assignmentName.text.toString()

                            var submit = Submit(moduleId,assignmentId,submitId,user.uid.toString(),currentTime.toString(),currentDate.toString(),"n/a","pending","$fName $lName",assignmentName,"0")
                            FirebaseDatabase.getInstance().getReference("Submit").child(submitId).setValue(submit).addOnSuccessListener {
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                binding.loaderLayout.visibility = View.GONE
                                binding.dataLayout.visibility = View.GONE
                                binding.successLayout.visibility = View.VISIBLE
                            }.addOnFailureListener{
                                binding.loaderLayout.visibility = View.GONE
                                binding.dataLayout.visibility = View.VISIBLE
                                binding.successLayout.visibility = View.GONE
                                Toast.makeText(this, "Failed to upload!", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }

                }
            }

        }else{
            Toast.makeText(this, "Upload your file to submit", Toast.LENGTH_SHORT).show()
            binding.loaderLayout.visibility = View.GONE
            binding.dataLayout.visibility = View.VISIBLE
            binding.successLayout.visibility = View.GONE
        }
    }


    private fun deleteSub(submitIndex:String){
        binding.deleteSubConfirm.visibility = View.VISIBLE
        binding.deleteSubCancel.visibility = View.VISIBLE
    }
    private fun deleteSubConfirm(submitIndex:String,assignmentId: String,moduleId: String,userType: String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileRef = storageRef.child("submit/$submitIndex")

        fileRef.delete()
            .addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Submit").child(submitIndex).removeValue().addOnSuccessListener {
                    var intent = Intent(this,ViewAssignment::class.java).also {
                        it.putExtra("assignmentId",assignmentId)
                        it.putExtra("userType",userType)
                        it.putExtra("moduleId",moduleId)
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.so_slide,R.anim.so_slide)
                    finish()
                }.addOnFailureListener{
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                    binding.loaderLayout.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
            }

    }

}