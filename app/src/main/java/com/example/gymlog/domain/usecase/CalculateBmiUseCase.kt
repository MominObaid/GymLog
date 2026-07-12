package com.example.gymlog.domain.usecase

import java.util.Locale
import javax.inject.Inject

class CalculateBmiUseCase @Inject constructor() {

    sealed class BmiResult {
        data class Success(
            val score: Float,
            val status: String,
            val colorRes: Int,
            val progress: Int
        ) : BmiResult()
        object InvalidInput : BmiResult()
    }

    operator fun invoke(heightCm: Float, weightKg: Float): BmiResult {
        if (heightCm <= 100 || weightKg <= 30) {
            return BmiResult.InvalidInput
        }

        val heightM = heightCm / 100
        val bmi = weightKg / (heightM * heightM)

        val (status, color, progress) = when {
            bmi < 18.5 -> Triple("Underweight", com.example.gymlog.R.color.workout_blue, 25)
            bmi < 25.0 -> Triple("Healthy", com.example.gymlog.R.color.health_green, 50)
            bmi < 30.0 -> Triple("Overweight", com.example.gymlog.R.color.streak_amber, 75)
            else -> Triple("Obese", com.example.gymlog.R.color.error_red, 100)
        }

        return BmiResult.Success(bmi, status, color, progress)
    }
}
