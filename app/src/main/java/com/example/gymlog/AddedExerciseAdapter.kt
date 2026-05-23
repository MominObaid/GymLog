package com.example.gymlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.model.RoutineExerciseEntity

class AddedExerciseAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<AddedExerciseAdapter.ViewHolder>() {

    private var items = mutableListOf<RoutineExerciseEntity>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.textViewExName)
        val detailsText: TextView = itemView.findViewById(R.id.textViewExDetails)
        val removeButton: ImageButton = itemView.findViewById(R.id.imageButtonRemove)

        init {
            removeButton.setOnClickListener {
                onRemoveClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_added_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameText.text = item.exerciseName
        holder.detailsText.text = "${item.targetSets} sets x ${item.targetReps} reps"
    }

    override fun getItemCount(): Int = items.size

    fun addExercise(exercise: RoutineExerciseEntity) {
        items.add(exercise)
        notifyItemInserted(items.size - 1)
    }

    fun removeExercise(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getExercises(): List<RoutineExerciseEntity> = items
}
