package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModuleAdapter(private val moduleList:ArrayList<Module>, private val listener: ViewModules): RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.module_element,parent,false)
        return ModuleViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val currentItem = moduleList[position]
        holder.name.text = "Module Name : ${currentItem.name.toString()}"
        holder.code.text = "module Code : ${currentItem.code.toString()}"
    }
    override fun getItemCount(): Int {
        return moduleList.size
    }

    inner class ModuleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.moduleElementName)
        val code: TextView = itemView.findViewById(R.id.moduleElementCode)

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