package com.example.gymlog

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.example.gymlog.model.Workout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@AndroidEntryPoint
class ProgressChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private val viewModel: WorkoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_chart)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.chartToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.outline_arrow_back_24)

        // viewModel is now injected via Hilt


        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: return
        findViewById<TextView>(R.id.textViewChartTitle).text = "$exerciseName Progress"
        lineChart = findViewById(R.id.lineChart)

        viewModel.getWorkoutHistory(exerciseName).observe(this) { workouts ->
            if (workouts.isNotEmpty()) {
                setupChart(workouts)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupChart(workouts: List<Workout>) {

        val isNightMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        val entries = workouts.mapIndexed { index, workout ->
            Entry(index.toFloat(), workout.weight.toFloat())
        }
        val dataSet = LineDataSet(entries, "Weight (kg)").apply {
            color = Color.CYAN
            valueTextColor = textColor
        }

        lineChart.apply {
            data = LineData(dataSet)
            lineChart.description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            animateX(1000)
            invalidate() // Refresh chart
        }
    }
}