package com.example.gymlog

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.gymlog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val workoutViewModel: WorkoutViewModel by viewModels{
        val database = WorkoutDatabase.getDatabase(this.application)
        val repository = WorkoutRepository(database.workoutDao())
        WorkoutViewModelFactory(repository)
    }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteAllConfirmationDialog()
//                Toast.makeText(this, "All workouts have been deleted", Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showDeleteAllConfirmationDialog(){
        AlertDialog.Builder(this)
            .setTitle("Delete All Workouts")
            .setMessage("Are you sure you want to delete everything? This cannot be undone.")
            .setPositiveButton("Yes, Delete All"){_,_ ->
                workoutViewModel.deleteAll()
                Toast.makeText(this,"All workouts deleted",Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No",null)
            .show()
    }
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