package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.gymlog.databinding.DialogAddExerciseToRoutineBinding
import com.example.gymlog.databinding.FragmentAddRoutineBinding
import com.example.gymlog.model.RoutineExerciseEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddRoutineFragment : Fragment() {

    private var _binding: FragmentAddRoutineBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private lateinit var adapter: AddedExerciseAdapter
    private val args: AddRoutineFragmentArgs by navArgs()
    private var routineId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineId = args.routineId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Toolbar with Back Button
        binding.toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        adapter = AddedExerciseAdapter(onRemoveClick = { position ->
            adapter.removeExercise(position)
        })
        binding.recyclerViewAddedExercises.adapter = adapter

        if (routineId != -1) {
            loadExistingRoutine()
        }

        binding.buttonAddExerciseToRoutine.setOnClickListener {
            showAddExerciseDialog()
        }

        binding.buttonSaveRoutine.setOnClickListener {
            saveRoutine()
        }
    }

    private fun loadExistingRoutine() {
        lifecycleScope.launch {
            val routine = viewModel.getRoutineById(routineId)
            routine?.let {
                binding.editTextRoutineName.setText(it.name)
                binding.editTextRoutineGoal.setText(it.goal)
                binding.editTextRestTimer.setText(it.restTimerSeconds.toString())
                binding.buttonSaveRoutine.text = getString(R.string.update_routine)
            }
        }
        viewModel.getExercisesForRoutine(routineId).observe(viewLifecycleOwner) { exercises ->
            if (exercises != null && adapter.itemCount == 0) {
                exercises.forEach { adapter.addExercise(it) }
            }
        }
    }

    private fun showAddExerciseDialog() {
        val dialogBinding = DialogAddExerciseToRoutineBinding.inflate(LayoutInflater.from(requireContext()))
        val exerciseNameAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        dialogBinding.autoCompleteExName.setAdapter(exerciseNameAdapter)

        workoutViewModel.apiExercises.observe(viewLifecycleOwner) { exercises ->
            exercises?.let {
                val names = it.mapNotNull { ex -> ex.name }
                exerciseNameAdapter.clear()
                exerciseNameAdapter.addAll(names)
                exerciseNameAdapter.notifyDataSetChanged()
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Exercise")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.autoCompleteExName.text.toString().trim()
                val setsText = dialogBinding.editTextExSets.text.toString()
                val repsText = dialogBinding.editTextExReps.text.toString()

                if (name.isNotEmpty() && setsText.isNotEmpty() && repsText.isNotEmpty()) {
                    val exercise = RoutineExerciseEntity(
                        routineId = if (routineId != -1) routineId else 0,
                        exerciseName = name,
                        targetSets = setsText.toInt(),
                        targetReps = repsText.toInt(),
                        exerciseOrder = adapter.itemCount
                    )
                    adapter.addExercise(exercise)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun saveRoutine() {
        val name = binding.editTextRoutineName.text.toString().trim()
        val goal = binding.editTextRoutineGoal.text.toString().trim()
        val restTimer = binding.editTextRestTimer.text.toString().toIntOrNull() ?: 90
        val exercises = adapter.getExercises()

        if (name.isNotEmpty() && exercises.isNotEmpty()) {
            if (routineId != -1) {
                viewModel.updateRoutine(routineId, name, goal, exercises, restTimer)
            } else {
                viewModel.insertRoutine(name, goal, exercises, restTimer)
            }
            findNavController().popBackStack()
        } else {
            Toast.makeText(requireContext(), "Routine name and exercises required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
