package com.example.gymlog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.ItemSessionExerciseBinding
import com.example.gymlog.databinding.ItemSessionSetBinding
import com.example.gymlog.model.RoutineExerciseEntity
import com.example.gymlog.model.SessionExerciseEntity

class SessionExerciseAdapter(
    private val onSetDone: () -> Unit
) : RecyclerView.Adapter<SessionExerciseAdapter.ViewHolder>() {

    private var exercises = emptyList<RoutineExerciseEntity>()
    private val sessionSets = mutableMapOf<Int, MutableList<SessionSetData>>()

    data class SessionSetData(
        var weight: Float = 0f,
        var reps: Int = 0,
        var isDone: Boolean = false
    )

    inner class ViewHolder(private val binding: ItemSessionExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(exercise: RoutineExerciseEntity) {
            binding.textViewExerciseName.text = exercise.exerciseName
            binding.textViewTarget.text = "Target: ${exercise.targetSets} sets x ${exercise.targetReps} reps"
            
            val sets = sessionSets.getOrPut(exercise.id) {
                MutableList(exercise.targetSets) { SessionSetData(reps = exercise.targetReps) }
            }

            renderSets(sets, exercise.id)

            binding.buttonAddSet.setOnClickListener {
                sets.add(SessionSetData(reps = exercise.targetReps))
                renderSets(sets, exercise.id)
            }
        }

        private fun renderSets(sets: MutableList<SessionSetData>, exerciseId: Int) {
            binding.layoutSets.removeAllViews()
            sets.forEachIndexed { index, setData ->
                val setBinding = ItemSessionSetBinding.inflate(
                    LayoutInflater.from(binding.layoutSets.context),
                    binding.layoutSets,
                    false
                )
                setBinding.textViewSetNumber.text = (index + 1).toString()
                
                // Initialize values
                setBinding.editTextWeight.setText(if (setData.weight > 0) setData.weight.toString() else "")
                setBinding.editTextReps.setText(if (setData.reps > 0) setData.reps.toString() else "")
                setBinding.checkBoxDone.isChecked = setData.isDone

                // Text Change Listeners - Update state immediately
                setBinding.editTextWeight.doAfterTextChanged {
                    setData.weight = it.toString().toFloatOrNull() ?: 0f
                }
                
                setBinding.editTextReps.doAfterTextChanged {
                    setData.reps = it.toString().toIntOrNull() ?: 0
                }

                setBinding.checkBoxDone.setOnCheckedChangeListener { _, isChecked ->
                    setData.isDone = isChecked
                    // Also ensure values are captured when checking
                    setData.weight = setBinding.editTextWeight.text.toString().toFloatOrNull() ?: 0f
                    setData.reps = setBinding.editTextReps.text.toString().toIntOrNull() ?: 0
                    if (isChecked) onSetDone()
                }
                
                binding.layoutSets.addView(setBinding.root)
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
        this.exercises = newExercises
        notifyDataSetChanged()
    }

    fun getSessionExercises(): List<SessionExerciseEntity> {
        val result = mutableListOf<SessionExerciseEntity>()
        exercises.forEach { exercise ->
            sessionSets[exercise.id]?.forEachIndexed { index, setData ->
                if (setData.isDone) {
                    result.add(
                        SessionExerciseEntity(
                            sessionId = 0, // Set later by repository
                            exerciseName = exercise.exerciseName,
                            setNumber = index + 1,
                            reps = setData.reps,
                            weight = setData.weight
                        )
                    )
                }
            }
        }
        return result
    }
}
