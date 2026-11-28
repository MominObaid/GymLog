package com.example.gymlog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.DialogExerciseGuideDetailBinding
import com.example.gymlog.databinding.FragmentExerciseGuideBinding
import com.example.gymlog.model.ExerciseGuide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExerciseGuideFragment : Fragment() {

    private var _binding: FragmentExerciseGuideBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var guideRepository: ExerciseGuideRepository

    private lateinit var adapter: ExerciseGuideAdapter
    private var selectedMuscleGroup: String = "All"
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilters()
        updateGuideList()
    }

    private fun setupRecyclerView() {
        adapter = ExerciseGuideAdapter(
            onFormClick = { guide ->
                showGuideDetailDialog(guide)
            },
            onVideoClick = { guide ->
                launchTechniqueVideo(guide)
            }
        )
        binding.rvExerciseGuides.apply {
            adapter = this@ExerciseGuideFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchAndFilters() {
        binding.etSearchGuide.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                updateGuideList()
            }
        })

        binding.chipGroupMuscles.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds.first())
                selectedMuscleGroup = chip?.text?.toString() ?: "All"
            } else {
                selectedMuscleGroup = "All"
            }
            updateGuideList()
        }
    }

    private fun updateGuideList() {
        val filtered = guideRepository.filterGuides(searchQuery, selectedMuscleGroup)
        adapter.setData(filtered)
        
        binding.layoutNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvExerciseGuides.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showGuideDetailDialog(guide: ExerciseGuide) {
        val dialogBinding = DialogExerciseGuideDetailBinding.inflate(LayoutInflater.from(requireContext()))
        
        dialogBinding.tvDetailName.text = guide.name
        dialogBinding.tvDetailMuscle.text = guide.muscleGroup
        dialogBinding.tvDetailEquipment.text = guide.equipment
        dialogBinding.tvDetailDifficulty.text = guide.difficulty
        dialogBinding.tvSetupText.text = guide.setupInstructions
        dialogBinding.tvExecutionText.text = guide.executionInstructions
        dialogBinding.tvProTipsText.text = guide.proTips
        dialogBinding.tvMistakesText.text = guide.commonMistakes

        dialogBinding.cardVideoBanner.setOnClickListener {
            launchTechniqueVideo(guide)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun launchTechniqueVideo(guide: ExerciseGuide) {
        try {
            // First try launching native YouTube app if installed
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:${guide.youtubeVideoId}"))
            startActivity(appIntent)
        } catch (e: Exception) {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.videoUrl))
            try {
                startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Unable to open video link.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
