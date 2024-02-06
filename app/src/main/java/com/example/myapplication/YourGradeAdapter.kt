package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YourGradeAdapter(private val gradeList:ArrayList<Grade>, private val listener: YourGrades): RecyclerView.Adapter<YourGradeAdapter.ModuleViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.grade_element,parent,false)
        return ModuleViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val currentItem = gradeList[position]
        holder.name.text = "${currentItem.moduleName.toString()}"
        holder.assignment.text = "${currentItem.assignmentName.toString()}"
        holder.marks.text = "${currentItem.marks.toString()}/${currentItem.outOff.toString()}"
    }
    override fun getItemCount(): Int {
        return gradeList.size
    }

    inner class ModuleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.moduleNameGrade)
        val assignment: TextView = itemView.findViewById(R.id.assignmentNameGrade)
        val marks: TextView = itemView.findViewById(R.id.marksGrade)

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