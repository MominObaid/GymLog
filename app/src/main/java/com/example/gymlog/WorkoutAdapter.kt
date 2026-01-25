package com.example.gymlog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {
    private var workoutList = emptyList<Workout>()
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
    override fun getItemCount(): Int{
        return workoutList.size
    }
    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int){
        val currentWorkout = workoutList[position]
        holder.nameTextView.text = currentWorkout.name
        holder.detailTextView.text = "${currentWorkout.sets} sets x ${currentWorkout.reps} reps @ ${currentWorkout.weight}kg"
        holder.dateTextView.text = currentWorkout.date
    }
    fun setData(workouts : List<Workout>){
        this.workoutList = workouts
        notifyDataSetChanged()
    }
    fun getWorkoutAt(position: Int): Workout{
        return workoutList[position]
    }
    interface OnItemClickListener{
        fun onItemClick(workout: Workout)
    }
}














