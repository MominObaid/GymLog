package com.example.gymlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {
    private var workouts = emptyList<Workout>()
    inner class WorkoutViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewExerciseName)
        val detailTextView: TextView = itemView.findViewById(R.id.textViewDetails)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val workout = getWorkoutAt(position)
                    listener.onItemClick(workout)
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout,parent,false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val current = workouts[position]
        holder.nameTextView.text = current.name
        holder.detailTextView.text =
            "${current.sets} sets x ${current.reps} reps @ ${current.weight}kg"
        holder.dateTextView.text = current.date

    }
    override fun getItemCount(): Int {
        return workouts.size
    }
    fun setData(workouts : List<Workout>){
        this.workouts = workouts
        notifyDataSetChanged()
    }
    fun getWorkoutAt(position: Int): Workout{
        return workouts[position]
    }
    interface OnItemClickListener{
        fun onItemClick(workout: Workout)
    }
}














