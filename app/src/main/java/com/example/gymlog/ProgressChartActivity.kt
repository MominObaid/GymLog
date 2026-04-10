package com.example.gymlog

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gymlog.api.RetrofitInstance
import com.example.gymlog.model.Workout
import com.example.gymlog.model.WorkoutDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ProgressChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var viewModel: WorkoutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_chart)

        val workoutDao = WorkoutDatabase.getDatabase(application).workoutDao()
        val repository = WorkoutRepository(workoutDao, RetrofitInstance.api)
        val factory = WorkoutViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WorkoutViewModel::class.java)


        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: return
        findViewById<TextView>(R.id.textViewChartTitle).text = "$exerciseName Progress"
        lineChart = findViewById(R.id.lineChart)

        viewModel.getWorkoutHistory(exerciseName).observe(this) { workouts ->
            if (workouts.isNotEmpty()) {
                setupChart(workouts)
            }
        }
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

//        val dataSet = LineDataSet(entries, "Weight (kg)").apply {
//            color = Color.BLUE
//            setCircleColor(Color.BLUE)
//            valueTextColor = Color.BLACK
//            lineWidth = 2f
//            circleRadius = 5f
//            setDrawCircleHole(false)
//            setDrawFilled(true) // Highlights the area under the line
//            fillColor = Color.CYAN
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//        }

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