package com.example.gymlog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.FragmentAnalyticsBinding
import com.example.gymlog.model.VolumePoint
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoutineViewModel by viewModels()
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure MainActivity FAB for AI Chat
        (activity as? MainActivity)?.configureFab(
            R.drawable.ic_ai_sparkles,
            "Ask AI",
            { (activity as? MainActivity)?.revealChat() }
        )

        setupSessionRecyclerView()

        viewModel.weeklyVolume.observe(viewLifecycleOwner) { volume ->
            binding.textViewWeeklyVolume.text = String.format(Locale.getDefault(), "%.1f kg", volume)
        }

        viewModel.volumeHistory.observe(viewLifecycleOwner) { history ->
            if (history.isNotEmpty()) {
                setupVolumeChart(history)
            }
        }

        viewModel.strongestExercises.observe(viewLifecycleOwner) { stats ->
            binding.layoutStrongestExercises.removeAllViews()
            stats.forEach { stat ->
                val textView = TextView(requireContext()).apply {
                    text = String.format(Locale.getDefault(), "%s: %.1f kg", stat.exerciseName, stat.maxWeight)
                    setPadding(0, 4, 0, 4)
                    textSize = 16f
                }
                binding.layoutStrongestExercises.addView(textView)
            }
        }

        viewModel.plateaus.observe(viewLifecycleOwner) { plateaus ->
            if (plateaus.isNotEmpty()) {
                binding.cardPlateaus.visibility = View.VISIBLE
                binding.layoutPlateaus.removeAllViews()
                plateaus.forEach { plateau ->
                    val textView = TextView(requireContext()).apply {
                        text = plateau
                        setPadding(0, 4, 0, 4)
                        textSize = 14f
                    }
                    binding.layoutPlateaus.addView(textView)
                }
            } else {
                binding.cardPlateaus.visibility = View.GONE
            }
        }
        
        // Use LiveData from Repository via ViewModel for sessions
        // We'll add this to RoutineViewModel if it's not there
    }

    private fun setupSessionRecyclerView() {
        sessionAdapter = SessionAdapter(onDeleteClick = { session ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Session?")
                .setMessage("Remove this session from history?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteSession(session.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        })
        
        binding.recyclerViewSessions.apply {
            adapter = sessionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        // Add swipe to delete for sessions too
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val session = sessionAdapter.getSessionAt(viewHolder.adapterPosition)
                viewModel.deleteSession(session.id)
                Snackbar.make(binding.root, "Session deleted", Snackbar.LENGTH_LONG).show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewSessions)
    }

    private fun setupVolumeChart(history: List<VolumePoint>) {
        val entries = history.mapIndexed { index, point ->
            Entry(index.toFloat(), point.volume)
        }

        val dataSet = LineDataSet(entries, "Total Volume").apply {
            color = Color.BLUE
            setDrawCircles(true)
            setDrawValues(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartVolumeHistory.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawLabels(false)
            animateY(1000)
            invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateMilestones()
        
        // Start observing sessions
        viewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.setData(sessions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
