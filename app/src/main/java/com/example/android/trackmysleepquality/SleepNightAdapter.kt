package com.example.android.trackmysleepquality

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight

class SleepNightAdapter: RecyclerView.Adapter<TextItemViewHolder>() {

    var data= listOf<SleepNight>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount()= data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        val mySleepNight= data[position]
        holder.textView.text= mySleepNight.sleepQuality.toString()
    }


}