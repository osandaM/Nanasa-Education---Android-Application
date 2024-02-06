package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssignmentAdapter(private val assignmentArray:ArrayList<AssignmentItem>, private val listener: Assignment): RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.assignment_element,parent,false)
        return AssignmentViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val currentItem = assignmentArray[position]
        holder.name.text = "${currentItem.name.toString()}"
        holder.description.text = "${currentItem.description.toString()}"
    }
    override fun getItemCount(): Int {
        return assignmentArray.size
    }

    inner class AssignmentViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.assignmentName)
        val description: TextView = itemView.findViewById(R.id.assignmentDescription)

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