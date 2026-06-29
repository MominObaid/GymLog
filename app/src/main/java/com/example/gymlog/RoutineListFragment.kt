package com.example.gymlog

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymlog.databinding.FragmentRoutineListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoutineListFragment : Fragment() {

    private var _binding: FragmentRoutineListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var adapter: RoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.allRoutines.observe(viewLifecycleOwner) { routines ->
            adapter.setData(routines)
            binding.layoutNoRoutines.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddRoutine.setOnClickListener {
            findNavController().navigate(R.id.addRoutineFragment)
        }

        binding.fabAiGenerate.setOnClickListener {
            showAiGenerateDialog()
        }

        viewModel.aiPlanGenerated.observe(viewLifecycleOwner) { generated ->
            if (generated) {
                Toast.makeText(requireContext(), "AI Workout Plan Added!", Toast.LENGTH_SHORT).show()
                viewModel.resetAiPlanFlag()
            }
        }
    }

    private fun showAiGenerateDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "e.g. 4 day muscle building plan"
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("AI Workout Generator")
            .setMessage("What kind of plan would you like?")
            .setView(input)
            .setPositiveButton("Generate") { _, _ ->
                val request = input.text.toString()
                if (request.isNotEmpty()) {
                    viewModel.generateAiPlan(request)
                    Toast.makeText(requireContext(), "Generating plan...", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = RoutineAdapter(
            onStartSessionClick = { routine ->
                val bundle = Bundle().apply {
                    putInt("routine_id", routine.id)
                    putString("routine_name", routine.name)
                }
                findNavController().navigate(R.id.workoutSessionFragment, bundle)
            },
            onDeleteClick = { routine ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Routine?")
                    .setMessage("Delete '${routine.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteRoutine(routine.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onItemClick = { routine ->
                val bundle = Bundle().apply {
                    putInt("routine_id", routine.id)
                }
                findNavController().navigate(R.id.addRoutineFragment, bundle)
            }
        )
        binding.recyclerViewRoutines.apply {
            adapter = this@RoutineListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
