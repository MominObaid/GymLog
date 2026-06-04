package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gymlog.databinding.FragmentProfileBinding
import com.example.gymlog.model.UserProfile
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        observeProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        // Hide primary FAB when on profile screen
        (activity as? MainActivity)?.configureFab(null, null, null)
    }

    private fun setupSpinners() {
        val goals = arrayOf("Muscle Gain", "Fat Loss", "Strength", "Endurance", "General Fitness")
        val goalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, goals)
        binding.spinnerGoal.setAdapter(goalAdapter)

        val levels = arrayOf("Beginner", "Intermediate", "Advanced", "Professional")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.spinnerLevel.setAdapter(levelAdapter)
    }

    private fun observeProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.etName.setText(it.name)
                binding.etAge.setText(if (it.age > 0) it.age.toString() else "")
                binding.etHeight.setText(if (it.height > 0) it.height.toString() else "")
                binding.etCurrentWeight.setText(if (it.currentWeight > 0) it.currentWeight.toString() else "")
                binding.etTargetWeight.setText(if (it.targetWeight > 0) it.targetWeight.toString() else "")
                binding.spinnerGoal.setText(it.goal, false)
                binding.spinnerLevel.setText(it.experienceLevel, false)
                binding.etWorkoutDays.setText(it.workoutDaysPerWeek.toString())
                binding.etEquipment.setText(it.availableEquipment)

                binding.tvProfileName.text = if (it.name.isNotBlank()) it.name else "Your Name"
                binding.tvProfileGoalHeader.text = if (it.goal.isNotBlank()) it.goal else "Set your goal"
            }
        }
        viewModel.loadProfile()
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f
        val currentWeight = binding.etCurrentWeight.text.toString().toFloatOrNull() ?: 0f
        val targetWeight = binding.etTargetWeight.text.toString().toFloatOrNull() ?: 0f
        val goal = binding.spinnerGoal.text.toString()
        val level = binding.spinnerLevel.text.toString()
        val days = binding.etWorkoutDays.text.toString().toIntOrNull() ?: 3
        val equipment = binding.etEquipment.text.toString()

        val profile = UserProfile(
            name = name,
            age = age,
            height = height,
            currentWeight = currentWeight,
            targetWeight = targetWeight,
            goal = goal,
            experienceLevel = level,
            workoutDaysPerWeek = days,
            availableEquipment = equipment
        )

        viewModel.updateProfile(profile)
        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        
        // Update header UI immediately
        binding.tvProfileName.text = if (name.isNotBlank()) name else "Your Name"
        binding.tvProfileGoalHeader.text = if (goal.isNotBlank()) goal else "Set your goal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
