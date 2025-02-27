package com.example.advandroidlab1.ui.content_provider.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.advandroidlab1.databinding.ItemEventBinding
import com.example.advandroidlab1.ui.content_provider.models.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarEventAdapter(
    private val onItemClick: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder>() {

    private var events: List<CalendarEvent> = emptyList()

    fun updateList(newEvents: List<CalendarEvent>) {
        val diffCallback = CalendarDiffCallback(events, newEvents)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        events = newEvents
        diffResult.dispatchUpdatesTo(this)
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CalendarEvent) {
            binding.tvTitle.text = event.title
            val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            binding.tvTime.text =
                "${formatter.format(Date(event.begin))} -- ${formatter.format(Date(event.end))}"

            if (!event.location.isNullOrEmpty()) {
                binding.tvLocation.text = event.location
                binding.tvLocation.visibility = View.VISIBLE
            } else {
                binding.tvLocation.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}

class CalendarDiffCallback(
    private val oldList: List<CalendarEvent>,
    private val newList: List<CalendarEvent>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id &&
                oldList[oldItemPosition].begin == newList[newItemPosition].begin
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
