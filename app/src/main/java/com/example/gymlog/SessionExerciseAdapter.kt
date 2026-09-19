package com.example.gymlog

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.ItemSessionExerciseBinding
import com.example.gymlog.databinding.ItemSessionSetBinding
import com.example.gymlog.model.RoutineExerciseEntity
import com.example.gymlog.model.WorkoutSetEntity

class SessionExerciseAdapter(
    private val onSetDone: () -> Unit,
    private val onSwapExercise: (String) -> Unit,
    private val onSetChanged: (WorkoutSetEntity) -> Unit,
    private val onAddSet: (String, String) -> Unit
) : RecyclerView.Adapter<SessionExerciseAdapter.ViewHolder>() {

    private var exercises = emptyList<RoutineExerciseEntity>()
    private var sessionSets = emptyList<WorkoutSetEntity>()
    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class ViewHolder(private val binding: ItemSessionExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(exercise: RoutineExerciseEntity) {
            binding.textViewExerciseName.text = exercise.exerciseName
            binding.textViewTarget.text = "Target: ${exercise.targetSets} sets x ${exercise.targetReps} reps"
            
            binding.btnSwapExercise.setOnClickListener {
                onSwapExercise(exercise.exerciseName)
            }

            updateSets()

            binding.buttonAddSet.setOnClickListener {
                onAddSet(exercise.exerciseName, exercise.muscleGroup)
            }
        }

        fun updateSets() {
            val exerciseName = binding.textViewExerciseName.text.toString()
            val sets = sessionSets.filter { it.exerciseName == exerciseName }
            val layout = binding.layoutSets
            
            // Reconcile views
            while (layout.childCount > sets.size) {
                layout.removeViewAt(layout.childCount - 1)
            }

            sets.forEachIndexed { index, setData ->
                val setBinding = if (index < layout.childCount) {
                    ItemSessionSetBinding.bind(layout.getChildAt(index))
                } else {
                    val newBinding = ItemSessionSetBinding.inflate(
                        LayoutInflater.from(layout.context),
                        layout,
                        false
                    )
                    layout.addView(newBinding.root)
                    newBinding
                }
                
                bindSet(setBinding, setData)
            }
        }

        private fun bindSet(setBinding: ItemSessionSetBinding, setData: WorkoutSetEntity) {
            setBinding.textViewSetNumber.text = setData.setNumber.toString()
            
            // Sync content only if NOT focused
            val weightStr = if (setData.weight > 0) setData.weight.toString() else ""
            if (!setBinding.editTextWeight.isFocused) {
                if (setBinding.editTextWeight.text.toString() != weightStr) {
                    setBinding.editTextWeight.setText(weightStr)
                }
            }

            val repsStr = if (setData.reps > 0) setData.reps.toString() else ""
            if (!setBinding.editTextReps.isFocused) {
                if (setBinding.editTextReps.text.toString() != repsStr) {
                    setBinding.editTextReps.setText(repsStr)
                }
            }

            // Style Gym-Friendly DONE button
            val context = setBinding.root.context
            if (setData.isCompleted) {
                setBinding.btnSetDone.setStrokeColorResource(R.color.health_green)
                setBinding.btnSetDone.setBackgroundColor(ContextCompat.getColor(context, R.color.health_green_light))
                setBinding.btnSetDone.setIconResource(R.drawable.outline_trophy_24)
                setBinding.btnSetDone.setIconTintResource(R.color.health_green)
            } else {
                setBinding.btnSetDone.setStrokeColorResource(R.color.card_stroke_color)
                setBinding.btnSetDone.setBackgroundColor(Color.TRANSPARENT)
                setBinding.btnSetDone.setIconResource(R.drawable.outline_work_history_24)
                setBinding.btnSetDone.setIconTintResource(R.color.card_stroke_color)
            }

            setupListeners(setBinding, setData)
        }

        private fun setupListeners(setBinding: ItemSessionSetBinding, setData: WorkoutSetEntity) {
            // Helper to get current live weight from view or setData
            fun getCurrentWeight(): Double {
                val text = setBinding.editTextWeight.text.toString().trim()
                return text.toDoubleOrNull() ?: setData.weight
            }

            // Helper to get current live reps from view or setData
            fun getCurrentReps(): Int {
                val text = setBinding.editTextReps.text.toString().trim()
                return text.toIntOrNull() ?: setData.reps
            }

            // Quick +/- Weight buttons (+2.5kg / -2.5kg)
            setBinding.btnMinusWeight.setOnClickListener {
                val currentWeight = getCurrentWeight()
                val currentReps = getCurrentReps()
                val newWeight = maxOf(0.0, currentWeight - 2.5)
                setBinding.editTextWeight.setText(if (newWeight > 0) newWeight.toString() else "")
                onSetChanged(setData.copy(weight = newWeight, reps = currentReps))
            }

            setBinding.btnPlusWeight.setOnClickListener {
                val currentWeight = getCurrentWeight()
                val currentReps = getCurrentReps()
                val newWeight = currentWeight + 2.5
                setBinding.editTextWeight.setText(newWeight.toString())
                onSetChanged(setData.copy(weight = newWeight, reps = currentReps))
            }

            // Quick +/- Reps buttons (+1 / -1)
            setBinding.btnMinusReps.setOnClickListener {
                val currentWeight = getCurrentWeight()
                val currentReps = getCurrentReps()
                val newReps = maxOf(0, currentReps - 1)
                setBinding.editTextReps.setText(if (newReps > 0) newReps.toString() else "")
                onSetChanged(setData.copy(weight = currentWeight, reps = newReps))
            }

            setBinding.btnPlusReps.setOnClickListener {
                val currentWeight = getCurrentWeight()
                val currentReps = getCurrentReps()
                val newReps = currentReps + 1
                setBinding.editTextReps.setText(newReps.toString())
                onSetChanged(setData.copy(weight = currentWeight, reps = newReps))
            }

            // Quick DONE Toggle Button
            setBinding.btnSetDone.setOnClickListener {
                val currentWeight = getCurrentWeight()
                val currentReps = getCurrentReps()
                val newCompleted = !setData.isCompleted
                onSetChanged(setData.copy(weight = currentWeight, reps = currentReps, isCompleted = newCompleted))
                if (newCompleted) onSetDone()
            }

            // Remove old text watchers
            (setBinding.editTextWeight.tag as? DebouncedTextWatcher)?.let { 
                setBinding.editTextWeight.removeTextChangedListener(it)
                setBinding.editTextWeight.removeCallbacks(it.updateRunnable)
            }
            (setBinding.editTextReps.tag as? DebouncedTextWatcher)?.let { 
                setBinding.editTextReps.removeTextChangedListener(it)
                setBinding.editTextReps.removeCallbacks(it.updateRunnable)
            }

            val weightWatcher = DebouncedTextWatcher(setBinding.editTextWeight) { newText ->
                val newWeight = newText.toDoubleOrNull() ?: 0.0
                val currentReps = getCurrentReps()
                if (newWeight != setData.weight) {
                    onSetChanged(setData.copy(weight = newWeight, reps = currentReps))
                }
            }
            
            val repsWatcher = DebouncedTextWatcher(setBinding.editTextReps) { newText ->
                val newReps = newText.toIntOrNull() ?: 0
                val currentWeight = getCurrentWeight()
                if (newReps != setData.reps) {
                    onSetChanged(setData.copy(weight = currentWeight, reps = newReps))
                }
            }

            setBinding.editTextWeight.addTextChangedListener(weightWatcher)
            setBinding.editTextWeight.tag = weightWatcher

            setBinding.editTextReps.addTextChangedListener(repsWatcher)
            setBinding.editTextReps.tag = repsWatcher
        }
    }

    private class DebouncedTextWatcher(
        private val view: android.view.View,
        private val onDebouncedChange: (String) -> Unit
    ) : TextWatcher {
        var updateRunnable: Runnable? = null
        private var lastProcessedText: String? = null

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString()
            if (view.isFocused && newText != lastProcessedText) {
                view.removeCallbacks(updateRunnable)
                updateRunnable = Runnable { 
                    lastProcessedText = newText
                    onDebouncedChange(newText) 
                }
                view.postDelayed(updateRunnable, 1000)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(exercises[position])
    }

    override fun getItemCount(): Int = exercises.size

    fun setData(newExercises: List<RoutineExerciseEntity>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = exercises.size
            override fun getNewListSize() = newExercises.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = exercises[oldPos].id == newExercises[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = exercises[oldPos] == newExercises[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.exercises = newExercises
        diffResult.dispatchUpdatesTo(this)
    }

    fun setSessionSets(newSets: List<WorkoutSetEntity>) {
        this.sessionSets = newSets
        
        recyclerView?.let { rv ->
            for (i in 0 until rv.childCount) {
                val child = rv.getChildAt(i)
                val holder = rv.getChildViewHolder(child) as? ViewHolder
                holder?.updateSets()
            }
        }
    }

    fun getSessionExercises(): List<WorkoutSetEntity> {
        return sessionSets.filter { it.isCompleted }
    }
}
