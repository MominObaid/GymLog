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
// this observeActiveSession function checks if there's an active workout session and updates the UI accordingly.
// If there's an active session, it shows the "Resume Session" button and sets its click listener to navigate to the active session.
// If there's no active session, it restores the original "Start Workout" button functionality and relies on the dashboard data observer to fill in the routine name and details.
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

        // Combine manual workouts and tracked sessions for a unified list
        workoutViewModel.allWorkouts.observe(viewLifecycleOwner) { manualWorkouts ->
            updateUnifiedRecentActivity(manualWorkouts, routineViewModel.recentSessionsWithRoutine.value)
        }

        routineViewModel.recentSessionsWithRoutine.observe(viewLifecycleOwner) { sessions ->
            updateUnifiedRecentActivity(workoutViewModel.allWorkouts.value, sessions)
        }
    }

    private fun updateUnifiedRecentActivity(manualWorkouts: List<Workout>?, sessions: List<com.example.gymlog.model.WorkoutSessionWithRoutine>?) {
        val sessionWorkouts = sessions?.map { item ->
            Workout(
                id = item.session.id,
                profileId = item.session.profileId,
                name = item.routine?.name ?: "Workout Session",
                sets = 0,
                reps = 0,
                weight = item.session.totalVolume,
                date = item.session.startTime
            )
        } ?: emptyList()

        val allWorkouts = (sessionWorkouts + (manualWorkouts ?: emptyList()))
            .sortedByDescending { it.date }
            .take(5)

        if (allWorkouts.isNotEmpty()) {
            adapter.setData(allWorkouts)
            binding.layoutEmptyState.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.VISIBLE
        }
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
            binding.tvGreeting.text = "${data.greeting}, ${data.userName}"
            
            val today = data.todayWorkout
            val isActive = sessionViewModel.uiState.value.session?.status == com.example.gymlog.model.WorkoutStatus.ACTIVE
            
            if (!isActive) {
                if (today != null) {
                    binding.tvHeaderSub.text = "Ready to crush ${today.routineName}?"
                    binding.tvTodayRoutineName.text = today.routineName
                    binding.tvTodayRoutineDetails.text = "${today.exerciseCount} Exercises • ${today.durationMinutes} min"
                    binding.cardTodayWorkout.visibility = View.VISIBLE
                    binding.btnStartTodayWorkout.text = getString(R.string.start_workout)
                } else if (data.allRoutinesCompleted) {
                    binding.tvHeaderSub.text = "You've crushed all your routines for today! "
                    binding.cardTodayWorkout.visibility = View.GONE
                } else {
                    binding.tvHeaderSub.text = "Start your fitness journey today!"
                    binding.cardTodayWorkout.visibility = View.GONE
                }
            }

            val stats = data.stats
            if (stats.topWeight > 0) {
                binding.tvRecordWeight.text = String.format(Locale.getDefault(), "%.1f kg", stats.topWeight)
                binding.tvRecordWeightLabel.text = stats.topExerciseName
            } else {
                binding.tvRecordWeight.text = "0 kg"
                binding.tvRecordWeightLabel.text = "Top Record"
            }
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
        routineViewModel.getCompletedSessionDetails(workout.id) { session, routine, sets ->
            if (session != null && sets.isNotEmpty()) {
                val name = routine?.name ?: workout.name ?: "Workout Session"
                showCompletedSessionDialog(
                    routineName = name,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    totalVolume = session.totalVolume,
                    notes = session.notes,
                    sets = sets
                )
            } else {
                val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                intent.putExtra("EXERCISE_NAME", workout.name)
                startActivity(intent)
            }
        }
    }

    private fun showCompletedSessionDialog(
        routineName: String,
        startTime: Long,
        endTime: Long,
        totalVolume: Double,
        notes: String?,
        sets: List<com.example.gymlog.model.WorkoutSetEntity>
    ) {
        val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy • HH:mm", Locale.getDefault())
        val durationMins = if (endTime > startTime) ((endTime - startTime) / 60000).toInt() else 0
        
        val messageBuilder = StringBuilder()
        messageBuilder.append(" Date: ").append(dateFormat.format(Date(startTime))).append("\n")
        if (durationMins > 0) {
            messageBuilder.append("️ Duration: ").append(durationMins).append(" mins\n")
        }
        messageBuilder.append("️ Total Volume: ").append(String.format(Locale.getDefault(), "%.1f kg", totalVolume)).append("\n\n")

        if (!notes.isNullOrBlank()) {
            messageBuilder.append(" Notes: ").append(notes).append("\n\n")
        }

        val grouped = sets.groupBy { it.exerciseName }
        if (grouped.isNotEmpty()) {
            messageBuilder.append("Exercises Completed:\n")
            grouped.forEach { (exName, exSets) ->
                messageBuilder.append("\n• ").append(exName).append(":\n")
                exSets.forEach { s ->
                    val status = if (s.isCompleted) "✓" else "-"
                    messageBuilder.append("   Set ").append(s.setNumber).append(": ")
                        .append(s.weight).append("kg x ").append(s.reps).append(" reps [").append(status).append("]\n")
                }
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(routineName)
            .setMessage(messageBuilder.toString())
            .setPositiveButton("Close", null)
            .show()
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
                    val setsCount = setsText.toIntOrNull() ?: 1
                    val repsCount = repsText.toIntOrNull() ?: 10
                    val weightVal = weightText.toDoubleOrNull() ?: 0.0

                    val workout = Workout(
                        name = name,
                        sets = setsCount,
                        reps = repsCount,
                        weight = weightVal,
                        date = selectedDateMillis
                    )
                    workoutViewModel.insert(workout)

                    // Also save as a completed WorkoutSession for unified streaks, volume, and 1RM analytics
                    val setsList = (1..setsCount).map { setNum ->
                        com.example.gymlog.model.WorkoutSetEntity(
                            sessionId = 0,
                            exerciseName = name,
                            muscleGroup = "Other",
                            setNumber = setNum,
                            weight = weightVal,
                            reps = repsCount,
                            isCompleted = true,
                            timestamp = selectedDateMillis
                        )
                    }

                    routineViewModel.saveWorkoutSession(
                        routineId = -1,
                        startTime = selectedDateMillis,
                        endTime = selectedDateMillis + 600000L,
                        notes = "Quick Log: $name",
                        sessionExercises = setsList
                    )

                    Toast.makeText(requireContext(), "Exercise Logged & Streaks Updated! 🔥", Toast.LENGTH_SHORT).show()
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