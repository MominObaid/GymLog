package com.example.gymlog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = WorkoutAdapter()
        binding.recyclerViewWorkouts.adapter = adapter
        binding.recyclerViewWorkouts.layoutManager = LinearLayoutManager(this)

        workoutViewModel.allWorkouts.observe(this, Observer { workouts ->
            workouts?.let { adapter.setData(it) }
        })
        binding.buttonAddExercise.setOnClickListener {
            addWorkout()
        }
    }
    private fun addWorkout(){
        val name = binding.editTextExerciseName.text.toString()
        val setsText = binding.editTextSets.text.toString()
        val repsText = binding.editTextReps.text.toString()
        val weightText = binding.editTextWeight.text.toString()

        if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()){
            Toast.makeText(this,"Please fill all fields ", Toast.LENGTH_SHORT).show()
            return
        }
        val currentDate = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())

        val workout = Workout(
            name = name,
            sets = setsText.toInt(),
            reps = repsText.toInt(),
            weight = weightText.toDouble(),
            date = currentDate
        )
        workoutViewModel.insert(workout)
        Toast.makeText(this,"Exercise Logged!", Toast.LENGTH_SHORT).show()
        clearInputFields()
    }
    private fun clearInputFields(){
        binding.editTextExerciseName.text.clear()
        binding.editTextSets.text.clear()
        binding.editTextReps.text.clear()
        binding.editTextWeight.text.clear()
        binding.editTextExerciseName.requestFocus()
    }
}
