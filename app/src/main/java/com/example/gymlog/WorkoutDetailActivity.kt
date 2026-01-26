package com.example.gymlog

//class WorkoutDetailActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityWorkoutDetailBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//    }
//}

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.gymlog.databinding.ActivityWorkoutDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutDetailBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private var currentWorkout: Workout? = null
    private var workoutId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar for Second Activity(WorkoutDetailActivity)
//        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back button

        // Get the workout ID from the intent
        workoutId = intent.getIntExtra("WORKOUT_ID", -1)
        if (workoutId == -1) {
            Toast.makeText(this, "Invalid Workout", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Observe the workout data from the ViewModel
        workoutViewModel.getWorkoutById(workoutId).observe(this, Observer { workout ->
            workout?.let {
                currentWorkout = it
                populateUI(it)
            }
        })

        // Set click listener for the update button
        binding.buttonUpdate.setOnClickListener {
            updateWorkout()
        }
    }

    private fun populateUI(workout: Workout) {
        binding.editTextExerciseNameDetail.setText(workout.name)
        binding.editTextSetsDetail.setText(workout.sets.toString())
        binding.editTextRepsDetail.setText(workout.reps.toString())
        binding.editTextWeightDetail.setText(workout.weight.toString())
    }

    private fun updateWorkout() {
        val name = binding.editTextExerciseNameDetail.text.toString().trim()
        val sets = binding.editTextSetsDetail.text.toString().toIntOrNull()
        val reps = binding.editTextRepsDetail.text.toString().toIntOrNull()
        val weight = binding.editTextWeightDetail.text.toString().toDoubleOrNull()

        if (name.isEmpty() || sets == null || reps == null || weight == null) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedWorkout = Workout(
            id = workoutId,
            name = name,
            sets = sets,
            reps = reps,
            weight = weight,
            date = ""     //you can set the date here or use the current date
        )

        workoutViewModel.update(updatedWorkout)
        Toast.makeText(this, "Workout Updated", Toast.LENGTH_SHORT).show()
        finish() // Go back to the main list
    }

    // --- Optional: Add a Delete button to the toolbar ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteConfirmationDialog()
                true
            }
            android.R.id.home -> { // Handle back button press
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout? This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                currentWorkout?.let {
                    workoutViewModel.delete(it)
                    Toast.makeText(this, "Workout Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .show()
    }
}
