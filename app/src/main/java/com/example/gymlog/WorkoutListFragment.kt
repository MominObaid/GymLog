package com.example.gymlog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
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
    private val routineViewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: WorkoutAdapter

    private val requestPermissions = registerForActivityResult(
        androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.isNotEmpty()) {
            routineViewModel.loadHealthData()
        }
    }

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
        setupMenu()
        updateGreetingAndDate()

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

        routineViewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.textViewStreak.text = streak.toString()
        }

        routineViewModel.totalWorkouts.observe(viewLifecycleOwner) { total ->
            binding.textViewTotalWorkouts.text = total.toString()
        }

        routineViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                if (it.name.isNotBlank()) {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val greeting = when (hour) {
                        in 0..11 -> "Good Morning"
                        in 12..16 -> "Good Afternoon"
                        else -> "Good Evening"
                    }
                    binding.textViewGreeting.text = "$greeting, ${it.name}!"
                }
            }
        }

        // Check Health Connect Availability
        val healthConnectManager = com.example.gymlog.health.HealthConnectManager(requireContext())
        if (healthConnectManager.isHealthConnectAvailable() && routineViewModel.todaySteps.value == null) {
            binding.buttonSyncHealth.visibility = View.VISIBLE
        }

        binding.cardStartWorkout.setOnClickListener {
            val options = arrayOf("Start from Routine", "Manual Log Entry")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("How do you want to train?")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> (activity as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_routines
                        1 -> showAddWorkoutDialog()
                    }
                }
                .show()
        }

        routineViewModel.todaySteps.observe(viewLifecycleOwner) { steps ->
            binding.layoutHealthSync.visibility = View.VISIBLE
            binding.textViewSteps.text = steps.toString()
            binding.buttonSyncHealth.visibility = View.GONE
        }

        routineViewModel.latestWeight.observe(viewLifecycleOwner) { weight ->
            weight?.let {
                binding.textViewWeight.text = String.format(Locale.getDefault(), "%.1f kg", it)
            }
        }

        binding.buttonSyncHealth.setOnClickListener {
            requestPermissions.launch(
                setOf(
                    HealthPermission.getReadPermission(androidx.health.connect.client.records.StepsRecord::class),
                    HealthPermission.getReadPermission(androidx.health.connect.client.records.WeightRecord::class),
                    HealthPermission.getWritePermission(androidx.health.connect.client.records.ExerciseSessionRecord::class)
                )
            )
        }

        setupItemTouchHelper()

        workoutViewModel.apiError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        // Configure MainActivity FAB for AI Chat on this fragment
        (activity as? MainActivity)?.configureFab(
            R.drawable.ic_ai_sparkles,
            "Ask AI",
            { (activity as? MainActivity)?.revealChat() }
        )

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

    private fun updateGreetingAndDate() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        
        binding.textViewGreeting.text = greeting
        
        val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        binding.textViewDateHeader.text = dateFormat.format(calendar.time)
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

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
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

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all -> {
                        workoutViewModel.deleteAll()
                        Toast.makeText(requireContext(), "All workouts have been deleted", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_settings -> {
                        (activity as? MainActivity)?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_profile
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

    override fun onResume() {
        super.onResume()
        routineViewModel.updateMilestones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
