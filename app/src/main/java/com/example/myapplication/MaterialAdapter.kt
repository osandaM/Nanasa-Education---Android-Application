package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MaterialAdapter(private val moduleList:ArrayList<Material>, private val listener: ModuleItemView): RecyclerView.Adapter<MaterialAdapter.ModuleViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.materail_element,parent,false)
        return ModuleViewHolder(itemView)

    }
    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val currentItem = moduleList[position]
        holder.name.text = "Material Name : ${currentItem.name.toString()}"
    }
    override fun getItemCount(): Int {
        return moduleList.size
    }

    inner class ModuleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val name: TextView = itemView.findViewById(R.id.materialElementName)

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