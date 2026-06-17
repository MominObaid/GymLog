package com.example.gymlog

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: SessionExerciseAdapter
    private var routineId: Int = -1
    private var routineName: String = ""
    private var restTimerSeconds: Int = 90
    private var startTime: Long = 0
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            routineId = it.getInt(ARG_ROUTINE_ID)
            routineName = it.getString(ARG_ROUTINE_NAME) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI Setup
        binding.toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener { 
            parentFragmentManager.popBackStack()
        }
        
        // Hide primary FAB during active session to avoid distractions

        binding.textViewSessionTitle.text = "Session: $routineName"
        startTime = System.currentTimeMillis()

        // Load routine for rest timer
        viewLifecycleOwner.lifecycleScope.launch {
            val routine = viewModel.getRoutineById(routineId)
            routine?.let {
                restTimerSeconds = it.restTimerSeconds
            }
        }

        setupRecyclerView()

        // Get exercises for this routine
        viewModel.getExercisesForRoutine(routineId).observe(viewLifecycleOwner) { exercises ->
            if (exercises == null) return@observe

            if (exercises.isEmpty()) {
                // If it's empty, we might be waiting for the DB to catch up
                // but since we now use transactions, this should only happen if it's actually empty
                binding.recyclerViewSessionExercises.visibility = View.GONE
                binding.textViewEmptySession.visibility = View.VISIBLE
            } else {
                binding.recyclerViewSessionExercises.visibility = View.VISIBLE
                binding.textViewEmptySession.visibility = View.GONE
                adapter.setData(exercises)
            }
        }

        viewModel.prEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("🏆 NEW PR!")
                    .setMessage(it)
                    .setPositiveButton("Awesome!") { dialog, _ ->
                        viewModel.resetPrEvent()
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        viewModel.sessionSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                viewModel.resetSessionSaved()
                parentFragmentManager.popBackStack()
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
        adapter = SessionExerciseAdapter(onSetDone = {
            startRestTimer(restTimerSeconds * 1000L)
        })
        binding.recyclerViewSessionExercises.apply {
            adapter = this@WorkoutSessionFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun startRestTimer(millis: Long) {
        countDownTimer?.cancel()
        binding.textViewTimer.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val display = String.format("Rest: %02d:%02d", seconds / 60, seconds % 60)
                binding.textViewTimer.text = display
            }

            override fun onFinish() {
                binding.textViewTimer.text = "Rest Over!"
                NotificationHelper.showNotification(
                    requireContext(),
                    NotificationHelper.REST_TIMER_CHANNEL_ID,
                    "Rest Finished",
                    "Time for your next set!",
                    1001
                )
            }
        }.start()
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
            viewModel.saveWorkoutSession(
                routineId = routineId,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                notes = null,
                sessionExercises = sessionExercises
            )
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Discard Session?")
                .setMessage("No sets were marked as done. Discard this session?")
                .setPositiveButton("Discard") { _, _ -> parentFragmentManager.popBackStack() }
                .setNegativeButton("Keep Logging", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        private const val ARG_ROUTINE_ID = "routine_id"
        private const val ARG_ROUTINE_NAME = "routine_name"

        fun newInstance(routineId: Int, routineName: String) =
            WorkoutSessionFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ROUTINE_ID, routineId)
                    putString(ARG_ROUTINE_NAME, routineName)
                }
            }
    }
}
