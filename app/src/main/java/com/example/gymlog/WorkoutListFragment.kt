package com.example.gymlog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.DialogAddWorkoutBinding
import com.example.gymlog.databinding.FragmentWorkoutListBinding
import com.example.gymlog.model.Workout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WorkoutListFragment : Fragment(), WorkoutAdapter.OnItemClickListener {

    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    private val workoutViewModel: WorkoutViewModel by viewModels()
    private lateinit var adapter: WorkoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        adapter = WorkoutAdapter(this)
        binding.recyclerViewWorkout.adapter = adapter
        binding.recyclerViewWorkout.layoutManager = LinearLayoutManager(requireContext())

        workoutViewModel.filteredWorkouts.observe(viewLifecycleOwner, Observer { workouts ->
            workouts?.let {
                if (it.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewWorkout.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.recyclerViewWorkout.visibility = View.VISIBLE
                    adapter.setData(it)
                }
            }
        })

        setupItemTouchHelper()

        binding.fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        workoutViewModel.apiError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        binding.chipAll.setOnClickListener {
            binding.chipRecent.isChecked = false
            binding.chipAll.isChecked = true
            workoutViewModel.updateFilter(clearDate = true)
        }

        binding.chipRecent.setOnClickListener {
            binding.chipAll.isChecked = false
            binding.chipRecent.isChecked = true
            workoutViewModel.updateFilter(sinceDate = getDaysAgo(7))
        }
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val workoutToDelete = adapter.getWorkoutAt(position)
                workoutViewModel.delete(workoutToDelete)
                Toast.makeText(requireContext(), "Exercise Deleted!", Toast.LENGTH_SHORT).show()

                Snackbar.make(
                    binding.root,
                    "${workoutToDelete.name} Deleted",
                    Snackbar.LENGTH_SHORT
                ).setAction("UNDO") {
                    workoutViewModel.insert(workoutToDelete)
                }.show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewWorkout)
    }

    private fun getDaysAgo(daysAgo: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return calendar.timeInMillis
    }

    override fun onItemClick(workout: Workout) {
        val options = arrayOf("View Details/ Edit", "View Progress Chart")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(workout.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
                        intent.putExtra("WORKOUT_ID", workout.id)
                        intent.putExtra("EXERCISE_NAME", workout.name)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(requireContext(), ProgressChartActivity::class.java)
                        intent.putExtra("EXERCISE_NAME", workout.name)
                        startActivity(intent)
                    }
                }
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Search Exercises...."

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                workoutViewModel.updateFilter(query = "")
                return true
            }
        })
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                workoutViewModel.updateFilter(query = newText.orEmpty())
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                workoutViewModel.deleteAll()
                Toast.makeText(requireContext(), "All workouts have been deleted", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddWorkoutDialog() {
        val dialogBinding = DialogAddWorkoutBinding.inflate(LayoutInflater.from(requireContext()))
        val exerciseNameAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        dialogBinding.autoCompleteExerciseName.setAdapter(exerciseNameAdapter)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var selectedDateMillis = calendar.timeInMillis
        dialogBinding.textViewDate.text = "Date: ${dateFormat.format(calendar.time)}"

        dialogBinding.textViewDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    dialogBinding.textViewDate.text = "Date: ${dateFormat.format(calendar.time)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        workoutViewModel.apiExercises.observe(viewLifecycleOwner, Observer { exercises ->
            exercises?.let {
                val names = it.mapNotNull { ex -> ex.name }
                exerciseNameAdapter.clear()
                exerciseNameAdapter.addAll(names)
                exerciseNameAdapter.notifyDataSetChanged()
            }
        })

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Workout")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.autoCompleteExerciseName.text.toString().trim()
                val setsText = dialogBinding.editTextSetsDialog.text.toString()
                val repsText = dialogBinding.editTextRepsDialog.text.toString()
                val weightText = dialogBinding.editTextWeightDialog.text.toString()

                if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                } else {
                    val workout = Workout(
                        name = name,
                        sets = setsText.toInt(),
                        reps = repsText.toInt(),
                        weight = weightText.toDouble(),
                        date = selectedDateMillis
                    )
                    workoutViewModel.insert(workout)
                    Toast.makeText(requireContext(), "Exercise Logged!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
