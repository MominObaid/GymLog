package com.example.gymlog

import android.view.LayoutInflater
import android.view.ViewGroup
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

    inner class RoutineViewHolder(private val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(routine: RoutineEntity) {
            binding.textViewRoutineName.text = routine.name
            binding.textViewRoutineGoal.text = routine.goal
            
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
        holder.bind(routines[position])
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
