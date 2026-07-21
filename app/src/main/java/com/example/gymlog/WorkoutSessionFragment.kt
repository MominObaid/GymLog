package com.example.gymlog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gymlog.service.RestTimerService
import com.example.gymlog.ui.session.SessionViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.FragmentWorkoutSessionBinding
import com.example.gymlog.utils.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutSessionFragment : Fragment() {

    private var _binding: FragmentWorkoutSessionBinding? = null
    private val binding get() = _binding!!
    private val routineViewModel: RoutineViewModel by activityViewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private lateinit var adapter: SessionExerciseAdapter
    private val args: WorkoutSessionFragmentArgs by navArgs()
    private var routineId: Int = -1
    private var routineName: String = ""
    private var restTimerSeconds: Int = 90

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                RestTimerService.ACTION_TIMER_TICK -> {
                    val millisLeft = intent.getLongExtra(RestTimerService.EXTRA_MILLIS_LEFT, 0L)
                    updateTimerDisplay(millisLeft)
                }
                RestTimerService.ACTION_TIMER_FINISHED -> {
                    binding.textViewTimer.text = getString(R.string.rest_over)
                }
            }
        }
    }

    private fun updateTimerDisplay(millisLeft: Long) {
        val seconds = millisLeft / 1000
        val display = String.format(java.util.Locale.getDefault(), "Rest: %02d:%02d", seconds / 60, seconds % 60)
        binding.textViewTimer.visibility = View.VISIBLE
        binding.textViewTimer.text = display
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineId = args.routineId
        routineName = args.routineName
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(RestTimerService.ACTION_TIMER_TICK)
            addAction(RestTimerService.ACTION_TIMER_FINISHED)
        }
        ContextCompat.registerReceiver(requireContext(), timerReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(timerReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI Setup
        binding.toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { 
            findNavController().popBackStack()
        }
        
        binding.textViewSessionTitle.text = getString(R.string.session_title_format, routineName)

        // Check if there's already an active session, if not start one
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.collect { state ->
                    if ((state.session == null) && !state.isLoading) {
                        val profileId = routineViewModel.userProfile.value?.id ?: 0
                        sessionViewModel.startWorkout(profileId, routineId)
                    }
                }
            }
        }

        // Load routine for rest timer
        viewLifecycleOwner.lifecycleScope.launch {
            val routine = routineViewModel.getRoutineById(routineId)
            routine?.let {
                restTimerSeconds = it.restTimerSeconds
            }
        }

        setupRecyclerView()

        // Get exercises for this routine
        routineViewModel.getExercisesForRoutine(routineId).observe(viewLifecycleOwner) { exercises ->
            if (exercises == null) return@observe

            if (exercises.isEmpty()) {
                binding.recyclerViewSessionExercises.visibility = View.GONE
                binding.textViewEmptySession.visibility = View.VISIBLE
            } else {
                binding.recyclerViewSessionExercises.visibility = View.VISIBLE
                binding.textViewEmptySession.visibility = View.GONE
                adapter.setData(exercises)
            }
        }

        // Observe sets from sessionViewModel and update adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.map { it.sets }.collect { sets ->
                    adapter.setSessionSets(sets)
                }
            }
        }

        // Observe session finished event
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.sessionFinished.collect { prMessage ->
                    // Show PR dialog if any
                    prMessage?.let {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("🏆 NEW PR!")
                            .setMessage(it)
                            .setPositiveButton("Awesome!", null)
                            .show()
                    }

                    // Trigger AI analysis if needed before closing
                    val completedSets = adapter.getSessionExercises()
                    if (completedSets.isNotEmpty()) {
                        routineViewModel.analyzeLastWorkout(
                            routineName,
                            completedSets,
                            0 // Duration can be calculated
                        )
                    }
                    findNavController().popBackStack()
                }
            }
        }

        // Observe rest timer from sessionViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionViewModel.uiState.map { it.session?.restTimerEndMillis ?: 0L }
                    .distinctUntilChanged()
                    .collect { endMillis ->
                        val now = System.currentTimeMillis()
                        if (endMillis > now) {
                            val duration = endMillis - now
                            updateTimerDisplay(duration) // Initial UI update
                            val intent = Intent(requireContext(), RestTimerService::class.java).apply {
                                putExtra(RestTimerService.EXTRA_DURATION_MILLIS, duration)
                            }
                            requireContext().startService(intent)
                        } else {
                            // If endMillis is 0 or in the past, ensure service is stopped
                            requireContext().stopService(Intent(requireContext(), RestTimerService::class.java))
                            binding.textViewTimer.visibility = View.GONE
                        }
                    }
            }
        }

        routineViewModel.prEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("🏆 NEW PR!")
                    .setMessage(it)
                    .setPositiveButton("Awesome!") { dialog, _ ->
                        routineViewModel.resetPrEvent()
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        routineViewModel.workoutAnalysis.observe(viewLifecycleOwner) { analysis ->
            analysis?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("AI Workout Analysis")
                    .setMessage(it)
                    .setPositiveButton("Got it!", null)
                    .show()
            }
        }

        routineViewModel.exerciseSubstitutions.observe(viewLifecycleOwner) { subs ->
            if (subs.isNotEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("AI Substitutions")
                    .setItems(subs.toTypedArray()) { _, which ->
                        Toast.makeText(requireContext(), "Try doing: ${subs[which]}", Toast.LENGTH_LONG).show()
                    }
                    .show()
            }
        }

        routineViewModel.sessionSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                // Trigger AI analysis for the dashboard
                val exercises = adapter.getSessionExercises()
                routineViewModel.analyzeLastWorkout(routineName, exercises, ((System.currentTimeMillis() - 0 /* startTime managed by session */) / 60000).toInt())
                
                routineViewModel.resetSessionSaved()
                findNavController().popBackStack()
            }
        }

        binding.textViewTimer.setOnClickListener {
            showChangeTimerDialog()
        }

        binding.buttonFinishSession.setOnClickListener {
            finishSession()
        }
    }

    private fun setupRecyclerView() {
        adapter = SessionExerciseAdapter(
            onSetDone = {
                sessionViewModel.startRestTimer(restTimerSeconds * 1000L)
            },
            onSwapExercise = { exerciseName ->
                routineViewModel.getSubstitutions(exerciseName)
            },
            onSetChanged = { set ->
                sessionViewModel.updateSet(set)
            },
            onAddSet = { exerciseName, muscleGroup ->
                sessionViewModel.addSet(exerciseName, muscleGroup, 0.0, 0)
            }
        )
        binding.recyclerViewSessionExercises.apply {
            adapter = this@WorkoutSessionFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showChangeTimerDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Seconds"
            setText(restTimerSeconds.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Rest Timer")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                restTimerSeconds = input.text.toString().toIntOrNull() ?: 90
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun finishSession() {
        val sessionExercises = adapter.getSessionExercises()
        if (sessionExercises.isNotEmpty()) {
            sessionViewModel.finishWorkout()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Discard Session?")
                .setMessage("No sets were marked as done. Discard this session?")
                .setPositiveButton("Discard") { _, _ -> 
                    // TODO: Logic to delete the empty session if it was created
                    findNavController().popBackStack() 
                }
                .setNegativeButton("Keep Logging", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
