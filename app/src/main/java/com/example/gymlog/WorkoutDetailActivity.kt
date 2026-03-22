package com.example.gymlog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gymlog.api.RetrofitInstance
import com.example.gymlog.databinding.ActivityWorkoutDetailBinding
import com.example.gymlog.model.Workout
import com.example.gymlog.model.WorkoutDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutDetailBinding
    private lateinit var workoutViewModel: WorkoutViewModel
    private var currentWorkout: Workout? = null
    private var workoutId: Int = -1
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.detailToolbar)


        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
        val apiService = RetrofitInstance.api
        val repository = WorkoutRepository(workoutDao, apiService)
        val factory = WorkoutViewModelFactory(repository)
        workoutViewModel = ViewModelProvider(this, factory).get(WorkoutViewModel::class.java)


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
        selectedDate = workout.date
        binding.textViewDate.text = selectedDate

        binding.textViewDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // If we already have a date, try to set the picker to that date
        val dateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = dateFormat.parse(selectedDate)
            if (date != null) calendar.time = date
        } catch (e: Exception) {
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
                binding.textViewDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
            date = selectedDate     //you can set the date here or use the current date
        )

        workoutViewModel.update(updatedWorkout)
        Toast.makeText(this, "Workout Updated", Toast.LENGTH_SHORT).show()
        finish() // Go back to the main list
    }

    // --- Optional: Add a Delete button to the toolbar ---

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
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
