package com.example.gymlog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.ItemWorkoutBinding
import com.example.gymlog.model.WorkoutSessionWithRoutine
import java.text.SimpleDateFormat
import java.util.*

class RecentSessionAdapter(
    private val onItemClick: (WorkoutSessionWithRoutine) -> Unit
) : RecyclerView.Adapter<RecentSessionAdapter.ViewHolder>() {

    private var sessions = emptyList<WorkoutSessionWithRoutine>()

    inner class ViewHolder(private val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WorkoutSessionWithRoutine) {
            binding.textViewExerciseName.text = item.routine?.name ?: "Workout Session"
            binding.textViewDetails.text = String.format(Locale.getDefault(), "Total Volume: %.1f kg", item.session.totalVolume)
            
            val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            binding.textViewDate.text = dateFormat.format(Date(item.session.startTime))
            
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun setData(newSessions: List<WorkoutSessionWithRoutine>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = sessions.size
            override fun getNewListSize() = newSessions.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = 
                sessions[oldPos].session.id == newSessions[newPos].session.id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = 
                sessions[oldPos] == newSessions[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.sessions = newSessions
        diffResult.dispatchUpdatesTo(this)
    }
}
