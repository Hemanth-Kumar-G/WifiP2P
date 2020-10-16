package com.hemanth.wifip2p

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hemanth.wifip2p.databinding.ReceivedDataItemBinding

class ReceivedDataAdapter(private val list: ArrayList<String>) :
    RecyclerView.Adapter<ReceivedDataAdapter.ReceivedDataViewHolder>() {

    class ReceivedDataViewHolder(val binding: ReceivedDataItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivedDataViewHolder {
        val binding =
            ReceivedDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceivedDataViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReceivedDataViewHolder, position: Int) {
        holder.binding.tvReceivedText.text = "$position :-  ${list.getOrNull(position)}"
    }
}