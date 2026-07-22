package com.example.gymlog

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.DialogAddWorkoutBinding
import com.example.gymlog.databinding.FragmentWorkoutListBinding
import com.example.gymlog.model.Workout
import com.example.gymlog.ui.session.SessionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class WorkoutListFragment : Fragment(), WorkoutAdapter.OnItemClickListener {

    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    private val workoutViewModel: WorkoutViewModel by activityViewModels()
    private val routineViewModel: RoutineViewModel by activityViewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
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

        setupRecentActivityList()
        setupClickListeners()
        observeDashboardData()
        observeActiveSession()
    }

    private fun observeActiveSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.collect { state ->
                    val session = state.session
                    if (session != null && session.status == com.example.gymlog.model.WorkoutStatus.ACTIVE) {
                        // Update Today's Workout Card to Resume Active Session
                        binding.cardTodayWorkout.visibility = View.VISIBLE
                        binding.tvTodayRoutineName.text = getString(R.string.active_workout)
                        binding.tvTodayRoutineDetails.text = getString(R.string.resume_session_desc)
                        binding.btnStartTodayWorkout.text = getString(R.string.resume_session)
                        
                        binding.btnStartTodayWorkout.setOnClickListener {
                            val action = WorkoutListFragmentDirections.actionWorkoutsToSession(session.routineId, "Active Session")
                            findNavController().navigate(action)
                        }
                    } else {
                        // Restore original listener and data will be handled by observeDashboardData
                        binding.btnStartTodayWorkout.text = getString(R.string.start_workout)
                        binding.btnStartTodayWorkout.setOnClickListener {
                            routineViewModel.todayWorkout.value?.let { today ->
                                val action = WorkoutListFragmentDirections.actionWorkoutsToSession(today.routineId, today.routineName)
                                findNavController().navigate(action)
                            } ?: run {
                                (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_routines
                            }
                        }
                        // Dashboard data observer will fill in the routine name/details
                    }
                }
            }
        }
    }

    private fun setupRecentActivityList() {
        adapter = WorkoutAdapter(this)
        binding.rvRecentActivity.adapter = adapter
        binding.rvRecentActivity.layoutManager = LinearLayoutManager(requireContext())

        // Observe manual workouts for recent activity
        workoutViewModel.allWorkouts.observe(viewLifecycleOwner, Observer { workouts ->
            if (!workouts.isNullOrEmpty()) {
                val recent = workouts.take(3)
                adapter.setData(recent)
                binding.layoutEmptyState.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.VISIBLE
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnQuickLog.setOnClickListener {
            showAddWorkoutDialog()
        }

        binding.btnAiCoach.setOnClickListener {
            (activity as? MainActivity)?.revealChat()
        }

        binding.tvViewAllActivity.setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_stats
        }
    }

    private fun observeDashboardData() {
        routineViewModel.dashboardHeader.observe(viewLifecycleOwner) { data ->
            binding.tvGreeting.text = "${data.greeting}, ${data.userName} 👋"
            
            val today = data.todayWorkout
            if (today != null && (sessionViewModel.uiState.value.session?.status != com.example.gymlog.model.WorkoutStatus.ACTIVE)) {
                binding.tvHeaderSub.text = "Ready to crush ${today.routineName}?"
                binding.tvTodayRoutineName.text = today.routineName
                binding.tvTodayRoutineDetails.text = "${today.exerciseCount} Exercises • ${today.durationMinutes} min"
                binding.cardTodayWorkout.visibility = View.VISIBLE
            } else if (today == null && (sessionViewModel.uiState.value.session?.status != com.example.gymlog.model.WorkoutStatus.ACTIVE)) {
                binding.tvHeaderSub.text = "Start your fitness journey today!"
                binding.cardTodayWorkout.visibility = View.GONE
            }

            val stats = data.stats
            binding.tvWeeklyVolume.text = String.format(Locale.getDefault(), "%.1f kg", stats.weeklyVolume)
            binding.tvWeeklyCount.text = stats.workoutCount.toString()
            binding.tvFavExercise.text = stats.favoriteExercise
        }

        routineViewModel.streak.observe(viewLifecycleOwner) { streak ->
            val best = routineViewModel.longestStreak.value ?: 0
            binding.tvStreakInfo.text = "Current: $streak Days | Best: $best"
        }

        routineViewModel.workoutAnalysis.observe(viewLifecycleOwner) { analysis ->
            analysis?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Coach's Post-Workout Review")
                    .setMessage(it)
                    .setPositiveButton("Thanks Coach!") { _, _ -> routineViewModel.resetAnalysis() }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    override fun onItemClick(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("WORKOUT_ID", workout.id)
        intent.putExtra("EXERCISE_NAME", workout.name)
        startActivity(intent)
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
            .setTitle("Quick Log Workout")
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
        routineViewModel.updateDashboardData()
        routineViewModel.updateMilestones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
