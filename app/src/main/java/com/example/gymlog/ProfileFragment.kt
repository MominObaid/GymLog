package com.example.gymlog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.FragmentProfileBinding
import com.example.gymlog.domain.usecase.CalculateBmiUseCase
import com.example.gymlog.model.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var profileAdapter: ProfilePillAdapter
    
    @Inject
    lateinit var calculateBmiUseCase: CalculateBmiUseCase

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

        setupProfileList()
        setupSpinners()
        observeProfile()
        setupLiveBmiCalculation()

        binding.btnAddProfile.setOnClickListener {
            createNewProfile()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnSyncNow.setOnClickListener {
            viewModel.syncData()
            Toast.makeText(requireContext(), "Syncing to cloud...", Toast.LENGTH_SHORT).show()
        }

        updateSyncStatus()
        viewModel.loadProfile()
    }

    private fun updateSyncStatus() {
        if (viewModel.isCloudEnabled) {
            binding.tvCloudStatus.text = "Cloud backup is enabled"
            binding.btnSyncNow.isEnabled = true
        } else {
            binding.tvCloudStatus.text = "Login to enable cloud backup"
            binding.btnSyncNow.isEnabled = false
        }
    }

    private fun setupProfileList() {
        profileAdapter = ProfilePillAdapter(
            onProfileClick = { profile ->
                viewModel.switchProfile(profile.id)
            },
            onLongClick = { profile ->
                if (!profile.isActive) {
                    showDeleteProfileDialog(profile)
                }
            }
        )
        binding.rvProfiles.apply {
            adapter = profileAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun createNewProfile() {
        val colors = listOf(0xFF1976D2.toInt(), 0xFF388E3C.toInt(), 0xFF7B1FA2.toInt(), 0xFFFF8F00.toInt())
        val profile = UserProfile(
            id = 0,
            name = "Athlete ${profileAdapter.itemCount + 1}",
            isActive = false,
            avatarColor = colors.random()
        )
        viewModel.updateProfile(profile)
        Toast.makeText(requireContext(), "New profile added! Tap to switch.", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteProfileDialog(profile: UserProfile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Profile?")
            .setMessage("Remove '${profile.name}' and all associated data?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProfile(profile)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        viewModel.allProfiles.observe(viewLifecycleOwner) { profiles ->
            profileAdapter.setData(profiles)
        }

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

                binding.tvProfileName.text = it.name.ifBlank { "Your Name" }
                binding.tvProfileGoalHeader.text = it.goal.ifBlank { "Set your goal" }
                binding.ivProfileAvatar.setBackgroundColor(it.avatarColor)
                binding.ivProfileAvatar.setStrokeColor(android.content.res.ColorStateList.valueOf(it.avatarColor))

                calculateAndDisplayBmi()
            }
        }
    }

    private fun setupLiveBmiCalculation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateAndDisplayBmi()
            }
        }
        binding.etHeight.addTextChangedListener(watcher)
        binding.etCurrentWeight.addTextChangedListener(watcher)
    }

    private fun calculateAndDisplayBmi() {
        val heightStr = binding.etHeight.text.toString()
        val weightStr = binding.etCurrentWeight.text.toString()

        val heightCm = heightStr.toFloatOrNull() ?: 0f
        val weightKg = weightStr.toFloatOrNull() ?: 0f

        when (val result = calculateBmiUseCase(heightCm, weightKg)) {
            is CalculateBmiUseCase.BmiResult.Success -> {
                binding.tvBmiScore.text = String.format(Locale.getDefault(), "%.1f", result.score)
                binding.tvBmiStatus.text = result.status
                binding.tvBmiStatus.setTextColor(ContextCompat.getColor(requireContext(), result.colorRes))
                binding.bmiProgressBar.progress = result.progress
                binding.bmiProgressBar.setIndicatorColor(ContextCompat.getColor(requireContext(), result.colorRes))
            }
            CalculateBmiUseCase.BmiResult.InvalidInput -> {
                resetBmiDisplay()
            }
        }
    }

    private fun resetBmiDisplay() {
        binding.tvBmiScore.text = "--"
        binding.tvBmiStatus.text = "--"
        binding.bmiProgressBar.progress = 0
    }

    private fun saveProfile() {
        val currentProfile = viewModel.userProfile.value
        val name = binding.etName.text.toString().trim()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val height = binding.etHeight.text.toString().toFloatOrNull() ?: 0f
        val currentWeight = binding.etCurrentWeight.text.toString().toFloatOrNull() ?: 0f
        val targetWeight = binding.etTargetWeight.text.toString().toFloatOrNull() ?: 0f
        val goal = binding.spinnerGoal.text.toString()
        val level = binding.spinnerLevel.text.toString()
        val days = binding.etWorkoutDays.text.toString().toIntOrNull() ?: 3
        val equipment = binding.etEquipment.text.toString()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = UserProfile(
            id = currentProfile?.id ?: 0,
            name = name,
            age = age,
            height = height,
            currentWeight = currentWeight,
            targetWeight = targetWeight,
            goal = goal,
            experienceLevel = level,
            workoutDaysPerWeek = days,
            availableEquipment = equipment,
            isActive = currentProfile?.isActive ?: true,
            avatarColor = currentProfile?.avatarColor ?: 0xFF1976D2.toInt()
        )

        viewModel.updateProfile(profile)
        Toast.makeText(requireContext(), "Profile updated! ✨", Toast.LENGTH_SHORT).show()
        
        // Update header UI immediately
        binding.tvProfileName.text = name
        binding.tvProfileGoalHeader.text = goal.ifBlank { "Set your goal" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
