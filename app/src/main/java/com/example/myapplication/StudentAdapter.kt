package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(private val studentList:ArrayList<Student>, private val listener: ViewStudents): RecyclerView.Adapter<StudentAdapter.StudentViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.student_element,parent,false)
        return StudentViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val currentItem = studentList[position]
        holder.name.text = "Student Name : ${currentItem.firstName.toString()} ${currentItem.lastName.toString()}"
        holder.id.text = "Nanasa ID : ${currentItem.nanasaId.toString()}"
    }
    override fun getItemCount(): Int {
        return studentList.size
    }

    inner class StudentViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.studentElementName)
        val id: TextView = itemView.findViewById(R.id.studentElementId)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position:Int = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }


}