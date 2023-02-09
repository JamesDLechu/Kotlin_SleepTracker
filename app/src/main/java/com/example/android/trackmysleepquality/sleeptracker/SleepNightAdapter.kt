package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val ITEM_VIEW_HEADER= 1
const val ITEM_VIEW_SLEEP= 2

class SleepNightAdapter(val clickListener: SleepNightListener): ListAdapter<DataItem, RecyclerView.ViewHolder>( SleepNightDiffCallback() ) {

    val adapterScope= CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            ITEM_VIEW_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_SLEEP -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when( getItem(position)!! ) {
            is DataItem.Header -> ITEM_VIEW_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_SLEEP
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder){
            is ViewHolder -> {
                val nightItem= getItem(position)!! as DataItem.SleepNightItem
                holder.bind(nightItem.sleepNight, clickListener)
            }
        }
    }

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {

            val items= when(list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    class ViewHolder(val binding: ListItemSleepNightBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SleepNight, clickListener: SleepNightListener) {
            binding.sleep= item
            binding.clickListener= clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = ListItemSleepNightBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(view)
            }
        }
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder{
                return TextViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.header, parent, false)
                )
            }
        }
    }
}

class SleepNightDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    fun onClick(sleepNight: SleepNight) = clickListener(sleepNight.nightId)
}

sealed class DataItem {
    data class SleepNightItem(val sleepNight: SleepNight): DataItem() {
        override val id: Long
            get() = sleepNight.nightId
    }

    object Header: DataItem() {
        override val id: Long
            get() = Long.MIN_VALUE
    }

    abstract val id: Long
}