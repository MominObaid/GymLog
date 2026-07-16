package com.example.gymlog.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gymlog.R
import com.example.gymlog.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: DashboardUiState) {
        binding.tvGreeting.text = "${state.greeting}, ${state.userName}"
        binding.tvStreak.text = "Stay consistent. You're on a ${state.streak}-day streak."
        
        state.todayPlan?.let { plan ->
            binding.cardTodayPlan.visibility = View.VISIBLE
            binding.tvRoutineName.text = plan.routineName
            binding.tvExerciseCount.text = "${plan.exerciseCount} Exercises"
            binding.tvEstimatedTime.text = "${plan.estimatedMinutes} min"
            binding.tvRecoveryScore.text = "Recovery Score: ${state.recoveryScore}%"
        } ?: run {
            binding.cardTodayPlan.visibility = View.GONE
        }

        binding.tvWeeklyGoalStatus.text = buildWeeklyGoalString(state.weeklyGoal)

        state.recentWorkout?.let { recent ->
            binding.cardRecentWorkout.visibility = View.VISIBLE
            binding.tvRecentName.text = "Workout" // Need to fetch routine name if possible
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            binding.tvRecentDate.text = sdf.format(Date(recent.startTime))
            val duration = (recent.endTime - recent.startTime) / (1000 * 60)
            binding.tvRecentDuration.text = "$duration min"
        } ?: run {
            binding.cardRecentWorkout.visibility = View.GONE
        }

        binding.tvCoachInsight.text = state.coachInsight
    }

    private fun buildWeeklyGoalString(goal: WeeklyGoal): String {
        val boxes = StringBuilder()
        for (i in 1..goal.total) {
            if (i <= goal.completed) boxes.append("■") else boxes.append("□")
        }
        return "$boxes  ${goal.completed} / ${goal.total} workouts completed"
    }

    private fun setupListeners() {
        binding.btnStartWorkout.setOnClickListener {
            val plan = viewModel.uiState.value.todayPlan ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putInt("routine_id", plan.routineId)
                putString("routine_name", plan.routineName)
            }
            findNavController().navigate(R.id.action_workouts_to_session, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
