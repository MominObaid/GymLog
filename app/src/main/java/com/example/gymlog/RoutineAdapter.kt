package com.example.gymlog

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.ItemRoutineBinding
import com.example.gymlog.model.RoutineEntity

class RoutineAdapter(
    private val onStartSessionClick: (RoutineEntity) -> Unit,
    private val onDeleteClick: (RoutineEntity) -> Unit,
    private val onItemClick: (RoutineEntity) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    private var routines = emptyList<RoutineEntity>()

    private val colors = intArrayOf(
        R.color.workout_blue,
        R.color.streak_amber,
        R.color.health_purple,
        R.color.health_green,
        R.color.md_theme_tertiary
    )

    inner class RoutineViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(routine: RoutineEntity, position: Int) {
            binding.textViewRoutineName.text = routine.name
            binding.textViewRoutineGoal.text = routine.goal
            
            // Set accent color based on position
            val colorRes = colors[position % colors.size]
            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.viewAccent.setBackgroundColor(color)
            
            binding.root.setOnClickListener {
                onItemClick(routine)
            }
            
            binding.buttonStartSession.setOnClickListener {
                onStartSessionClick(routine)
            }
            
            binding.buttonDeleteRoutine.setOnClickListener {
                onDeleteClick(routine)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        holder.bind(routines[position], position)
    }

    override fun getItemCount(): Int = routines.size

    fun setData(newRoutines: List<RoutineEntity>) {
        val diffCallback = RoutineDiffCallback(this.routines, newRoutines)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.routines = newRoutines
        diffResult.dispatchUpdatesTo(this)
    }

    fun getRoutineAt(position: Int): RoutineEntity {
        return routines[position]
    }

    class RoutineDiffCallback(
        private val oldList: List<RoutineEntity>,
        private val newList: List<RoutineEntity>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
