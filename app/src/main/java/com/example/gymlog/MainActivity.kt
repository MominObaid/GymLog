package com.example.gymlog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.api.RetrofitInstance
import com.example.gymlog.databinding.ActivityMainBinding
import com.example.gymlog.databinding.DialogAddWorkoutBinding
import com.example.gymlog.model.Workout
import com.example.gymlog.model.WorkoutDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), WorkoutAdapter.OnItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var workoutViewModel: WorkoutViewModel //by viewModels()
    private var currentWorkouts: List<Workout> = emptyList()
    private var currentSearchQuery: String = ""
    private lateinit var adapter: WorkoutAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = WorkoutAdapter(this)
        binding.recyclerViewWorkout.adapter = adapter
        binding.recyclerViewWorkout.layoutManager = LinearLayoutManager(this)

        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
        val apiService = RetrofitInstance.api
        val repository = WorkoutRepository(workoutDao, apiService)
        val factory = WorkoutViewModelFactory(repository)
        workoutViewModel = ViewModelProvider(this, factory).get(WorkoutViewModel::class.java)

        workoutViewModel.fetchExercisesFromApi()
        //Observe the LiveData from the ViewModel.

        workoutViewModel.allWorkouts.observe(this, Observer { workouts ->
            workouts?.let {
                currentWorkouts = it
                applyFilter()
            }
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
                val workoutToDelete = adapter.getWorkoutAt(position)
//                    (binding.recyclerViewWorkout.adapter as WorkoutAdapter).getWorkoutAt(position)
                workoutViewModel.delete(workoutToDelete)
                Toast.makeText(this@MainActivity, "Exercise Deleted!", Toast.LENGTH_SHORT).show()

                Snackbar.make(
                    binding.root,
                    "${workoutToDelete.name} Deleted",
                    Snackbar.LENGTH_SHORT
                ).setAction("UNDO") {
                    workoutViewModel.insert(workoutToDelete)
                }.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWorkout)

        binding.fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }
        workoutViewModel.apiError.observe(this, Observer { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })
        binding.chipAll.setOnClickListener {
            binding.chipRecent.isChecked = false
            binding.chipAll.isChecked = true
            applyFilter()
        }
        binding.chipRecent.setOnClickListener {
            binding.chipAll.isChecked = false
            binding.chipRecent.isChecked = true
            applyFilter()
        }
    }

    private fun applyFilter() {
        var filteredList = if (binding.chipRecent.isChecked) {
            val sevenDaysAgo = getDaysAgo(7)
            currentWorkouts.filter { it.date >= sevenDaysAgo }
        } else {
            //show Everything
            currentWorkouts
        }
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { workout ->
                workout.name.orEmpty().contains(currentSearchQuery, ignoreCase = true)

            }
        }
        //update UI based on the filtered result
        if (filteredList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewWorkout.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewWorkout.visibility = View.VISIBLE
            adapter.setData(filteredList)
        }
    }

    private fun getDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
//
//    private var isNavigating= false
//
//    override fun onResume() {
//        super.onResume()
//        isNavigating = false
//        }

    override fun onItemClick(workout: Workout) {
//        if (isNavigating) return
//        isNavigating = true
        val option = arrayOf("View Details/ Edit ", "View Progress Chart")

        MaterialAlertDialogBuilder(this)
            .setTitle(workout.name)
            .setItems(option) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, WorkoutDetailActivity::class.java)
                        intent.putExtra("WORKOUT_ID", workout.id)
                        intent.putExtra("EXERCISE_NAME", workout.name)
                        startActivity(intent)
                    }

                    1 -> {
                        val intent = Intent(this, ProgressChartActivity::class.java)
//                        intent.putExtra("WORKOUT_ID", workout.id)
                        intent.putExtra("EXERCISE_NAME", workout.name)
                        startActivity(intent)
                    }
                }
            }
//            .setOnCancelListener {
//                isNavigating = false
//            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Search Exercises...."

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // When user clicks the "back" arrow or closes the search,
                currentSearchQuery = "" //reset the search query
                applyFilter()          //apply the filter with empty query means Show all workout again
                return true         // Allow the search bar to collapse back to an icon.
            }
        })
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus() //this Hide keyboard on sumbit
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText.orEmpty()
                applyFilter()
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                workoutViewModel.deleteAll()
                Toast.makeText(this, "All workouts have been deleted", Toast.LENGTH_SHORT)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogBinding = DialogAddWorkoutBinding.inflate(LayoutInflater.from(this))

        //Create an empty adapter for now, We will update it when the data arrives.
        val exerciseNameAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mutableListOf())
        dialogBinding.autoCompleteExerciseName.setAdapter(exerciseNameAdapter)
//        dialogBinding.autoCompleteExerciseName.setOnItemClickListener { _, _, position, _ ->

//        var selectedDate = ""
//        val calendar = Calendar.getInstance()
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        selectedDate = dateFormat.format(calendar.time)
//        dialogBinding.textViewDate.text = selectedDate
//
//        dialogBinding.textViewDate.setOnClickListener {
//            val datePickerDialog = DatePickerDialog(
//                this,
//                {_, year, month, dayOfMonth ->
//                    calendar.set(year,month,dayOfMonth)
//                    selectedDate = dateFormat.format(calendar.time)
//                    dialogBinding.textViewDate.text = selectedDate
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//            )
//            datePickerDialog.show()
//        }

        // Set initial date to today
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var selectedDate = dateFormat.format(calendar.time)
        dialogBinding.textViewDate.text = "Date: $selectedDate"

        dialogBinding.textViewDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    // Update the calendar object with user selection
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Format and update the variable and UI
                    selectedDate = dateFormat.format(calendar.time)
                    dialogBinding.textViewDate.text = "Date: $selectedDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        //Observe the API exercise LiveData
        workoutViewModel.apiExercises.observe(this, Observer { exercises ->
            exercises?.let {
                //When data is Fetched update the adapter
                val exerciseName = it.map { exercises -> exercises.name }
                exerciseNameAdapter.clear()
                exerciseNameAdapter.addAll(exerciseName)
                exerciseNameAdapter.notifyDataSetChanged()
            }
        })

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Workout")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.autoCompleteExerciseName.text.toString().trim()
                val setsText = dialogBinding.editTextSetsDialog.text.toString()
                val repsText = dialogBinding.editTextRepsDialog.text.toString()
                val weightText = dialogBinding.editTextWeightDialog.text.toString()

                if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {

                    val workout = Workout(
                        name = name,
                        sets = setsText.toInt(),
                        reps = repsText.toInt(),
                        weight = weightText.toDouble(),
                        date = selectedDate
                    )
                    workoutViewModel.insert(workout)
                    Toast.makeText(this, "Exercise Logged!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
