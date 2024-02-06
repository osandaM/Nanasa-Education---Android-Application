package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.myapplication.databinding.ActivityViewSubmissionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

class ViewSubmission : AppCompatActivity() {

    private lateinit var binding:ActivityViewSubmissionBinding
    private lateinit var user:FirebaseAuth

    private var assignmentNamePublic = ""
    private var studentIndex = ""
    private var moduleNamePublic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewSubmissionBinding.inflate(layoutInflater)
        user = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val submitId = intent.getStringExtra("submitId")
        val status = intent.getStringExtra("status")
        val moduleId = intent.getStringExtra("moduleId")
        val assignmentId = intent.getStringExtra("assignmentId")

        readData(submitId.toString(),status.toString())

        binding.download.setOnClickListener{
            download(submitId.toString())
        }
        binding.giveMarks.setOnClickListener{
            giveMarks(submitId.toString(),assignmentId.toString(),assignmentNamePublic,moduleId.toString(),studentIndex.toString(),moduleNamePublic)
        }
        binding.refresh.setOnClickListener{
            readData(submitId.toString(),status.toString())
        }
        binding.back.setOnClickListener{
            var intent = Intent(this,EditAssignment::class.java).also {
                it.putExtra("assignmentId",assignmentId.toString())
                it.putExtra("moduleId",moduleId.toString())
            }
            startActivity(intent)
        }
    }

    private fun readData(submitId:String,status:String){
        binding.loaderLayout.visibility = View.VISIBLE
        binding.dataLayout.visibility = View.GONE
        binding.successLayout.visibility = View.GONE

        FirebaseDatabase.getInstance().getReference("Submit").child(submitId).get().addOnSuccessListener {
            if(it.exists()){
                var assignmentId = it.child("assignmentId").value.toString()
                var assignmentName = it.child("assignmentName").value.toString()
                var date = it.child("date").value.toString()
                var marks = it.child("marks").value.toString()
                var outOf = it.child("outOf").value.toString()
                var moduleId = it.child("moduleId").value.toString()
                var status = it.child("status").value.toString()
                var studentId = it.child("studentId").value.toString()
                var studentName = it.child("studentName").value.toString()
                var time = it.child("time").value.toString()

                binding.assignmentName.text = assignmentName
                studentIndex = studentId
                assignmentNamePublic = assignmentName
                FirebaseDatabase.getInstance().getReference("Module").child(moduleId).get().addOnSuccessListener {
                    binding.moduleName.text = it.child("name").value.toString()
                    moduleNamePublic = it.child("name").value.toString()

                    FirebaseDatabase.getInstance().getReference("Student").child(studentId).get().addOnSuccessListener {
                        binding.studentName.text = "Name : $studentName"
                        binding.nanasaId.text = "Nanasa ID : ${it.child("nanasaId").value.toString()}"

                        if(status == "pending"){
                            //not graded
                            binding.addMarksLayout.visibility = View.VISIBLE
                            binding.marksAddedLayout.visibility = View.GONE

                        }else if(status == "done"){
                            //graded
                            binding.marksAddedMarks.text = marks.toString()
                            binding.marksAddedOutOf.text = outOf.toString()
                            binding.addMarksLayout.visibility = View.GONE
                            binding.marksAddedLayout.visibility = View.VISIBLE
                        }
                        binding.loaderLayout.visibility = View.GONE
                        binding.dataLayout.visibility = View.VISIBLE

                    }
                }

            }else{
                Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun download(submitId: String){
        val storageRef = FirebaseStorage.getInstance().reference.child("submit/$submitId")
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

    private fun giveMarks(submitId: String,assignmentId:String,assignmentName:String,moduleId:String,studentId:String,moduleNamePublic:String){
        var marks = binding.numOfMarks.text
        var outOf = binding.outOf.text

        if(marks.isNotEmpty() && outOf.isNotEmpty()){
            binding.successLayout.visibility = View.GONE
            binding.loaderLayout.visibility = View.VISIBLE
            binding.dataLayout.visibility = View.GONE

            FirebaseDatabase.getInstance().getReference("Submit").child("$submitId/marks").setValue(marks.toString()).addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("Submit").child("$submitId/outOf").setValue(outOf.toString()).addOnSuccessListener{
                    FirebaseDatabase.getInstance().getReference("Submit").child("$submitId/status").setValue("done").addOnSuccessListener{

                        var gradeId = UUID.randomUUID()
                        var grade = Grade(marks.toString(),outOf.toString(),assignmentName.toString(),moduleId,assignmentId.toString(),studentId.toString(),gradeId.toString(),
                            moduleNamePublic)

                        FirebaseDatabase.getInstance().getReference("Grade").child(gradeId.toString()).setValue(grade).addOnSuccessListener {
                            binding.loaderLayout.visibility = View.GONE
                            binding.dataLayout.visibility = View.GONE
                            binding.successLayout.visibility = View.VISIBLE
                        }.addOnFailureListener{
                            binding.loaderLayout.visibility = View.GONE
                            binding.dataLayout.visibility = View.VISIBLE
                            Toast.makeText(this, "Successful with errors", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener{
                        binding.loaderLayout.visibility = View.GONE
                        binding.dataLayout.visibility = View.VISIBLE
                        Toast.makeText(this, "Successful with errors", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener{
                    binding.loaderLayout.visibility = View.GONE
                    binding.dataLayout.visibility = View.VISIBLE
                    Toast.makeText(this, "Successful with errors", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                binding.loaderLayout.visibility = View.GONE
                binding.dataLayout.visibility = View.VISIBLE
                Toast.makeText(this, "Failed to give marks", Toast.LENGTH_SHORT).show()
            }

        }
    }

}