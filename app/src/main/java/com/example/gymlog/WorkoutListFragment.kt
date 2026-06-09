package com.example.gymlog

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.FragmentWorkoutListBinding
import com.example.gymlog.model.Workout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class WorkoutListFragment : Fragment(), WorkoutAdapter.OnItemClickListener {

    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    private val workoutViewModel: WorkoutViewModel by viewModels()
    private val routineViewModel: RoutineViewModel by viewModels()
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

        // Configure MainActivity FAB
        (activity as? MainActivity)?.configureFab(
            R.drawable.ic_ai_sparkles,
            "Ask AI",
            { (activity as? MainActivity)?.revealChat() }
        )
    }

    private fun setupRecentActivityList() {
        adapter = WorkoutAdapter(this)
        binding.rvRecentActivity.adapter = adapter
        binding.rvRecentActivity.layoutManager = LinearLayoutManager(requireContext())

        // Observe manual workouts for recent activity
        // We'll show the last 3 from workoutViewModel
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
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_workouts
            // In a better flow, this might open the dialog directly
        }

        binding.btnAiCoach.setOnClickListener {
            (activity as? MainActivity)?.revealChat()
        }

        binding.tvViewAllActivity.setOnClickListener {
            (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_stats
        }

        binding.btnStartTodayWorkout.setOnClickListener {
            routineViewModel.todayWorkout.value?.let { today ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, WorkoutSessionFragment.newInstance(today.routineId, today.routineName))
                    .addToBackStack(null)
                    .commit()
            } ?: run {
                // If no routine, go to routines tab
                (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.nav_routines
            }
        }
    }

    private fun observeDashboardData() {
        routineViewModel.dashboardHeader.observe(viewLifecycleOwner) { header ->
            binding.tvGreeting.text = "${header.greeting}, ${header.userName} 👋"
        }

        routineViewModel.todayWorkout.observe(viewLifecycleOwner) { today ->
            if (today != null) {
                binding.tvHeaderSub.text = "Ready to crush ${today.routineName}?"
                binding.tvTodayRoutineName.text = today.routineName
                binding.tvTodayRoutineDetails.text = "${today.exerciseCount} Exercises • ${today.durationMinutes} min"
                binding.cardTodayWorkout.visibility = View.VISIBLE
            } else {
                binding.tvHeaderSub.text = "Start your fitness journey today!"
                binding.cardTodayWorkout.visibility = View.GONE
            }
        }

        routineViewModel.streak.observe(viewLifecycleOwner) { streak ->
            val best = routineViewModel.longestStreak.value ?: 0
            binding.tvStreakInfo.text = "Current: $streak Days | Best: $best"
        }

        routineViewModel.dashboardStats.observe(viewLifecycleOwner) { stats ->
            binding.tvWeeklyVolume.text = String.format(Locale.getDefault(), "%.1f kg", stats.weeklyVolume)
            binding.tvWeeklyCount.text = stats.workoutCount.toString()
            binding.tvFavExercise.text = stats.favoriteExercise
        }
    }

    override fun onItemClick(workout: Workout) {
        val intent = Intent(requireContext(), WorkoutDetailActivity::class.java)
        intent.putExtra("WORKOUT_ID", workout.id)
        intent.putExtra("EXERCISE_NAME", workout.name)
        startActivity(intent)
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
