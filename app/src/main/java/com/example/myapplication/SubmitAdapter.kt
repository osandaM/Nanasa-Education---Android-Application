package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubmitAdapter(private val subList:ArrayList<Submit>, private val listener: Submissions): RecyclerView.Adapter<SubmitAdapter.ModuleViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.submit_element,parent,false)
        return ModuleViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val currentItem = subList[position]
        holder.name.text = "${currentItem.studentName.toString()}"
        holder.moduleName.text = "${currentItem.assignmentName.toString()}"
    }
    override fun getItemCount(): Int {
        return subList.size
    }

    inner class ModuleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.submitEL_studentName)
        val moduleName: TextView = itemView.findViewById(R.id.submitEL_moduleName)

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