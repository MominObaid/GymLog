package com.example.gymlog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.FragmentAnalyticsBinding
import com.example.gymlog.model.MuscleVolume
import com.example.gymlog.model.OneRMPoint
import com.example.gymlog.model.VolumePoint
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        setupSessionRecyclerView()
        setupClickListeners()
        setupSpinners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.btnPredictProgress.setOnClickListener {
            binding.textViewPredictions.text = "Consulting the coach..."
            viewModel.predictProgress()
        }
    }

    private fun setupSpinners() {
        val commonLifts = arrayOf("Bench Press", "Squat", "Deadlift", "Overhead Press")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, commonLifts)
        binding.spinnerStrengthExercise.setAdapter(adapter)
        
        binding.spinnerStrengthExercise.setOnItemClickListener { _, _, _, _ ->
            val exercise = binding.spinnerStrengthExercise.text.toString()
            updateStrengthChart(exercise)
        }
    }

    private fun updateStrengthChart(exerciseName: String) {
        viewModel.getOneRMHistory(exerciseName).observe(viewLifecycleOwner) { history ->
            if (history != null && history.isNotEmpty()) {
                setupOneRMChart(history)
            }
        }
    }

    private fun observeData() {
        viewModel.weeklyVolume.observe(viewLifecycleOwner) { volume ->
            binding.textViewWeeklyVolume.text = String.format(Locale.getDefault(), "%.1f kg", volume ?: 0f)
        }

        viewModel.volumeHistory.observe(viewLifecycleOwner) { history ->
            if (!history.isNullOrEmpty()) {
                setupVolumeChart(history)
            }
        }

        viewModel.strongestExercises.observe(viewLifecycleOwner) { stats ->
            binding.layoutStrongestExercises.removeAllViews()
            stats?.forEach { stat ->
                val textView = TextView(requireContext()).apply {
                    text = String.format(Locale.getDefault(), "%s: %.1f kg", stat.exerciseName, stat.maxWeight)
                    setPadding(0, 4, 0, 4)
                    textSize = 16f
                }
                binding.layoutStrongestExercises.addView(textView)
            }
        }

        viewModel.plateaus.observe(viewLifecycleOwner) { plateaus ->
            if (!plateaus.isNullOrEmpty()) {
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

        viewModel.progressPredictions.observe(viewLifecycleOwner) { predictions ->
            predictions?.let {
                binding.textViewPredictions.text = it
                binding.btnPredictProgress.visibility = View.GONE
            }
        }

        viewModel.recoveryScore.observe(viewLifecycleOwner) { score ->
            binding.tvRecoveryScore.text = String.format(Locale.getDefault(), "%d%%", score)
            val color = when {
                score > 80 -> R.color.health_green
                score > 50 -> R.color.streak_amber
                else -> R.color.error_red
            }
            binding.tvRecoveryScore.setTextColor(ContextCompat.getColor(requireContext(), color))
        }

        viewModel.fatigueScore.observe(viewLifecycleOwner) { score ->
            binding.tvFatigueScore.text = String.format(Locale.getDefault(), "%d%%", score)
            val color = when {
                score < 40 -> R.color.health_green
                score < 70 -> R.color.streak_amber
                else -> R.color.error_red
            }
            binding.tvFatigueScore.setTextColor(ContextCompat.getColor(requireContext(), color))
        }

        viewModel.muscleVolume.observe(viewLifecycleOwner) { volume ->
            if (volume.isNotEmpty()) {
                setupMuscleChart(volume)
            }
        }

        viewModel.allSessions.observe(viewLifecycleOwner) { sessions ->
            if (sessions != null) {
                sessionAdapter.setData(sessions)
            }
        }

        // Initial load of strength chart
        updateStrengthChart("Bench Press")
    }

    private fun setupMuscleChart(data: List<MuscleVolume>) {
        val entries = data.mapIndexed { index, item ->
            BarEntry(index.toFloat(), item.value.toFloat())
        }

        val dataSet = BarDataSet(entries, "Sets per Muscle Group").apply {
            color = ContextCompat.getColor(requireContext(), R.color.workout_blue)
            valueTextSize = 10f
        }

        binding.chartMuscleVolume.apply {
            this.data = BarData(dataSet)
            description.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.label })
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    private fun setupOneRMChart(history: List<OneRMPoint>) {
        val entries = history.mapIndexed { index, point ->
            Entry(index.toFloat(), point.oneRM.toFloat())
        }

        val dataSet = LineDataSet(entries, "Estimated 1RM (kg)").apply {
            color = ContextCompat.getColor(requireContext(), R.color.streak_amber)
            setDrawCircles(true)
            setDrawValues(false)
            lineWidth = 2.5f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartStrengthTrend.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
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
            isNestedScrollingEnabled = false
        }
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val session = sessionAdapter.getSessionAt(position)
                viewModel.deleteSession(session.id)
                Snackbar.make(binding.root, "Session deleted", Snackbar.LENGTH_LONG).show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewSessions)
    }

    private fun setupVolumeChart(history: List<VolumePoint>) {
        try {
            val entries = history.mapIndexed { index, point ->
                Entry(index.toFloat(), point.volume)
            }

            val dataSet = LineDataSet(entries, "Total Volume").apply {
                color = Color.parseColor("#7B1FA2")
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = Color.parseColor("#7B1FA2")
                fillAlpha = 50
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateMilestones()
        viewModel.updateAdvancedAnalytics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
