package com.example.gymlog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.ActivityMainBinding
import com.example.gymlog.databinding.DialogAddWorkoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), WorkoutAdapter.OnItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private val workoutViewModel: WorkoutViewModel by viewModels()
//        WorkoutViewModelFactory(application)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val adapter = WorkoutAdapter(this)
        binding.recyclerViewWorkout.adapter = adapter
        binding.recyclerViewWorkout.layoutManager = LinearLayoutManager(this)

        workoutViewModel.fetchExercisesFromApi()

        workoutViewModel.allWorkouts.observe(this, Observer { workouts ->
            workouts?.let { adapter.setData(it) }
        })
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workoutToDelete =
                    (binding.recyclerViewWorkout.adapter as WorkoutAdapter).getWorkoutAt(position)
                workoutViewModel.delete(workoutToDelete)
                Toast.makeText(this@MainActivity, "Exercise Deleted!", Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWorkout)

        binding.fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }
        workoutViewModel.apiError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onItemClick(workout: Workout) {
        val intent = Intent(this, WorkoutDetailActivity::class.java)
        intent.putExtra("WORKOUT_ID", workout.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                workoutViewModel.deleteAll()
                Toast.makeText(this, "All workouts have been deleted", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showAddWorkoutDialog(){
        val dialogBinding = DialogAddWorkoutBinding.inflate(LayoutInflater.from(this))

        //Create an empty adapter for now, We will update it when the data arrives.
        val exerciseNameAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        dialogBinding.autoCompleteExerciseName.setAdapter(exerciseNameAdapter)

        //Observe the API exercise LiveData
        workoutViewModel.apiExercises.observe(this, Observer { exercises ->
            exercises?.let {
                //When data is Fetched update the adapter
                val exerciseName = it.map { exercises -> exercises.exerciseName }
                exerciseNameAdapter.clear()
                exerciseNameAdapter.addAll(exerciseName)
                exerciseNameAdapter.notifyDataSetChanged()
            }
        })

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Workout")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add"){_,_ ->
                val name = dialogBinding.autoCompleteExerciseName.text.toString().trim()
                val setsText = dialogBinding.editTextSetsDialog.text.toString()
                val repsText = dialogBinding.editTextRepsDialog.text.toString()
                val weightText = dialogBinding.editTextWeightDialog.text.toString()

                if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()){
                    Toast.makeText(this,"Please fill all fields", Toast.LENGTH_SHORT).show()
                }else{
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date())
                    val workout =Workout(
                        name = name,
                        sets = setsText.toInt(),
                        reps = repsText.toInt(),
                        weight = weightText.toDouble(),
                        date = currentDate
                    )
                    workoutViewModel.insert(workout)
                    Toast.makeText(this,"Exercise Logged!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
//    private fun addWorkout() {
//        val name = binding.editTextExerciseName.text.toString()
//        val setsText = binding.editTextSets.text.toString()
//        val repsText = binding.editTextReps.text.toString()
//        val weightText = binding.editTextWeight.text.toString()
//
//        if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()) {
//            Toast.makeText(this, "Please fill all fields ", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val currentDate = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())
//
//        val workout = Workout(
//            name = name,
//            sets = setsText.toInt(),
//            reps = repsText.toInt(),
//            weight = weightText.toDouble(),
//            date = currentDate
//        )
//        workoutViewModel.insert(workout)
//        Toast.makeText(this, "Exercise Logged!", Toast.LENGTH_SHORT).show()
////        clearInputFields()
//    }
//    private fun clearInputFields() {
//        binding.editTextExerciseName.clear()
//        binding.editTextSets.text.clear()
//        binding.editTextReps.text.clear()
//        binding.editTextWeight.text.clear()
//        binding.editTextExerciseName.requestFocus()
//    }
}
//        val adapter = WorkoutAdapter()
//       binding.recyclerViewWorkouts.adapter = adapter
//        binding.recyclerViewWorkouts.layoutManager = LinearLayoutManager(this)
//
//        workoutViewModel.allWorkouts.observe(this, Observer { workouts ->
//            workouts?.let { adapter.setData(it) }
//        })
//        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
//            0,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                val workoutToDelete =
//                    (binding.recyclerViewWorkouts.adapter as WorkoutAdapter).getWorkoutAt(position)
//                workoutViewModel.delete(workoutToDelete)
//                Toast.makeText(this@MainActivity, "Exercise Deleted!", Toast.LENGTH_SHORT).show()
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
//        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWorkouts)
//
//        binding.buttonAddExercise.setOnClickListener {
//            addWorkout()
//        }
//    }
//    private fun addWorkout() {
//        val name = binding.editTextExerciseName.text.toString()
//        val setsText = binding.editTextSets.text.toString()
//        val repsText = binding.editTextReps.text.toString()
//        val weightText = binding.editTextWeight.text.toString()
//
//        if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()) {
//            Toast.makeText(this, "Please fill all fields ", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val currentDate = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())
//
//        val workout = Workout(
//            name = name,
//            sets = setsText.toInt(),
//            reps = repsText.toInt(),
//            weight = weightText.toDouble(),
//            date = currentDate
//        )
//        workoutViewModel.insert(workout)
//        Toast.makeText(this, "Exercise Logged!", Toast.LENGTH_SHORT).show()
//        clearInputFields()
//    }
//
//    private fun clearInputFields() {
//        binding.editTextExerciseName.text.clear()
//        binding.editTextSets.text.clear()
//        binding.editTextReps.text.clear()
//        binding.editTextWeight.text.clear()
//        binding.editTextExerciseName.requestFocus()
//    }
//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 github login Token Expires on Sun, Feb 15 2026.