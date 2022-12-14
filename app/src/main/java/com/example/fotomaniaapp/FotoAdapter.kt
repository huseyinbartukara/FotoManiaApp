package com.example.fotomaniaapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fotomaniaapp.databinding.CardViewTasarimBinding

class FotoAdapter(val fotoList: ArrayList<Foto>) : RecyclerView.Adapter<FotoAdapter.FotoHolder>() {

    class FotoHolder(val binding : CardViewTasarimBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoHolder {
        val binding = CardViewTasarimBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FotoHolder(binding)
    }

    override fun onBindViewHolder(holder: FotoHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = fotoList.get(position).name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,PhotoActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",fotoList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return fotoList.size
    }
}