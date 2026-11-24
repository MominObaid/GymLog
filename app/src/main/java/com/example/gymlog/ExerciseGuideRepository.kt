package com.example.gymlog

import com.example.gymlog.model.ExerciseGuide
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseGuideRepository @Inject constructor() {

    private val exercises = listOf(
        ExerciseGuide(
            id = 1,
            name = "Barbell Bench Press",
            muscleGroup = "Chest",
            equipment = "Barbell & Bench",
            difficulty = "Intermediate",
            setupInstructions = "Lie flat on the bench with feet firm on the floor. Grip the bar slightly wider than shoulder-width with wrists straight and shoulder blades retracted.",
            executionInstructions = "Unrack the bar and lower it in a controlled arc to mid-chest. Press upward explosively while driving through the feet until arms are locked out.",
            proTips = "Keep elbows tucked at 45 degrees relative to your torso to protect shoulder joints. Drive feet into the ground for leg drive.",
            commonMistakes = "Flaring elbows 90 degrees out, bouncing the bar off the chest, lifting lower back excessively.",
            videoUrl = "https://www.youtube.com/watch?v=rT7DgCr-3pg",
            youtubeVideoId = "rT7DgCr-3pg"
        ),
        ExerciseGuide(
            id = 2,
            name = "Barbell Back Squat",
            muscleGroup = "Legs",
            equipment = "Barbell & Squat Rack",
            difficulty = "Advanced",
            setupInstructions = "Position bar across upper trapezius muscles. Step out with feet shoulder-width apart, toes pointed slightly outward (15-30 degrees).",
            executionInstructions = "Hinge at hips and bend knees simultaneously. Lower hips until thighs are at or below parallel with the floor. Drive through mid-foot to stand up.",
            proTips = "Brace core tightly using 360-degree belly breath before starting descending phase. Keep knees tracking over toes.",
            commonMistakes = "Knees caving inward (valgus collapse), rounding lower back (butt wink), rising onto toes.",
            videoUrl = "https://www.youtube.com/watch?v=ultWZbUMPL8",
            youtubeVideoId = "ultWZbUMPL8"
        ),
        ExerciseGuide(
            id = 3,
            name = "Conventional Deadlift",
            muscleGroup = "Back",
            equipment = "Barbell",
            difficulty = "Advanced",
            setupInstructions = "Stand with feet hip-width apart, barbell over mid-foot. Bend at hips to grip bar just outside knees with arms straight.",
            executionInstructions = "Pull chest up and flatten back. Wedging hips, pull bar upward close to shins and thighs until standing tall with shoulders back.",
            proTips = "Treat it as pushing the floor away with your legs rather than pulling with your lower back. Keep bar against your body.",
            commonMistakes = "Rounding lumbar spine, bar floating away from body, hyperextending lower back at lockout.",
            videoUrl = "https://www.youtube.com/watch?v=op9kVnSso6Q",
            youtubeVideoId = "op9kVnSso6Q"
        ),
        ExerciseGuide(
            id = 4,
            name = "Overhead Barbell Press",
            muscleGroup = "Shoulders",
            equipment = "Barbell",
            difficulty = "Intermediate",
            setupInstructions = "Rack bar at upper chest height. Grip bar shoulder-width apart with forearms vertical under the bar, glutes and core squeezed tight.",
            executionInstructions = "Tilt head back slightly as bar passes chin, then press straight overhead until arms lockout. Tilt head forward into neutral position at top.",
            proTips = "Squeeze glutes and abs hard throughout the lift to create a rigid torso and prevent lower back arching.",
            commonMistakes = "Excessive back arching, pressing bar forward instead of straight vertical line over mid-foot.",
            videoUrl = "https://www.youtube.com/watch?v=2yjwXTZQDDI",
            youtubeVideoId = "2yjwXTZQDDI"
        ),
        ExerciseGuide(
            id = 5,
            name = "Lat Pulldown",
            muscleGroup = "Back",
            equipment = "Cable Machine",
            difficulty = "Beginner",
            setupInstructions = "Sit at machine with thigh pads securing legs tightly. Grip wide bar with thumbs over bar or overhand grip wider than shoulders.",
            executionInstructions = "Lean back slightly (10-15 degrees). Pull bar downward toward upper chest by driving elbows down and back toward hip bones.",
            proTips = "Focus on pulling with your elbows rather than gripping hard with hands. Squeeze lats hard at bottom position.",
            commonMistakes = "Swinging torso backward for momentum, pulling bar behind neck, rounding shoulders forward.",
            videoUrl = "https://www.youtube.com/watch?v=CAwf7n6Luuc",
            youtubeVideoId = "CAwf7n6Luuc"
        ),
        ExerciseGuide(
            id = 6,
            name = "Incline Dumbbell Press",
            muscleGroup = "Chest",
            equipment = "Dumbbells & Bench",
            difficulty = "Intermediate",
            setupInstructions = "Set bench at 30-45 degree incline. Kick dumbbells up onto shoulders using knees and plant feet firmly on floor.",
            executionInstructions = "Press dumbbells upward over chest. Lower in controlled motion until dumbbells reach upper chest level with elbows at 45 degrees.",
            proTips = "A 30-degree incline targets upper chest better than steep 60-degree inclines which shift work onto front deltoids.",
            commonMistakes = "Setting bench incline too steep (>45 deg), letting dumbbells drift outward or arching lower back off bench.",
            videoUrl = "https://www.youtube.com/watch?v=8iPEnn-ltC8",
            youtubeVideoId = "8iPEnn-ltC8"
        ),
        ExerciseGuide(
            id = 7,
            name = "Dumbbell Lateral Raise",
            muscleGroup = "Shoulders",
            equipment = "Dumbbells",
            difficulty = "Beginner",
            setupInstructions = "Stand tall holding dumbbells at sides with palms facing inward, elbows slightly bent (10-15 degrees).",
            executionInstructions = "Raise arms out to sides in slight forward angle (scaption plane) until upper arms are parallel to floor. Slowly lower down.",
            proTips = "Lead movement with elbows rather than hands. Imagine pouring water out of a pitcher at top of movement.",
            commonMistakes = "Shrugging shoulders up to ears, swinging torso for momentum, raising dumbbells above shoulder height.",
            videoUrl = "https://www.youtube.com/watch?v=3VcKaXpzqRo",
            youtubeVideoId = "3VcKaXpzqRo"
        ),
        ExerciseGuide(
            id = 8,
            name = "Bulgarian Split Squat",
            muscleGroup = "Legs",
            equipment = "Bench & Dumbbells",
            difficulty = "Intermediate",
            setupInstructions = "Stand 2-3 feet in front of bench. Place top of back foot on bench. Hold dumbbells at sides with chest upright.",
            executionInstructions = "Lower hips by bending front knee until front thigh is parallel to floor. Drive through front heel to return to top.",
            proTips = "Lean torso slightly forward at 15 degrees to hit glutes more, or keep torso fully vertical to bias quadriceps.",
            commonMistakes = "Front foot too close or too far from bench, front knee collapsing inward, letting front heel rise.",
            videoUrl = "https://www.youtube.com/watch?v=2C-uNgKwPLE",
            youtubeVideoId = "2C-uNgKwPLE"
        ),
        ExerciseGuide(
            id = 9,
            name = "Dumbbell Bicep Curl",
            muscleGroup = "Arms",
            equipment = "Dumbbells",
            difficulty = "Beginner",
            setupInstructions = "Stand tall with feet shoulder-width, holding dumbbells at sides with palms facing inward (supinated or neutral grip).",
            executionInstructions = "Keeping upper arms stationary against ribs, curl weights upward while rotating palms to face ceiling at top. Squeeze bicep.",
            proTips = "Keep elbows locked in place at sides—do not let elbows drift forward which transfers tension to front shoulders.",
            commonMistakes = "Swinging hips for momentum, moving upper arm forward, dropping weight quickly without eccentric control.",
            videoUrl = "https://www.youtube.com/watch?v=ykJmrZ5v0Oo",
            youtubeVideoId = "ykJmrZ5v0Oo"
        ),
        ExerciseGuide(
            id = 10,
            name = "Tricep Rope Pushdown",
            muscleGroup = "Arms",
            equipment = "Cable Machine & Rope",
            difficulty = "Beginner",
            setupInstructions = "Attach rope to high pulley. Grip rope ends with palms facing each other. Stand upright with knees soft and elbows glued to ribs.",
            executionInstructions = "Extend arms downward toward thighs, spreading rope ends apart at bottom. Squeeze triceps hard before controlling back up.",
            proTips = "Spread rope ends out at full extension to maximize lateral head tricep peak contraction.",
            commonMistakes = "Flaring elbows away from ribs, using body weight to lean over rope, letting elbows move up and down.",
            videoUrl = "https://www.youtube.com/watch?v=vB5OHsJ3EME",
            youtubeVideoId = "vB5OHsJ3EME"
        ),
        ExerciseGuide(
            id = 11,
            name = "Hanging Leg Raise",
            muscleGroup = "Core",
            equipment = "Pull-Up Bar",
            difficulty = "Intermediate",
            setupInstructions = "Hang from pull-up bar with overhand grip, shoulders engaged, body still without swinging.",
            executionInstructions = "Raise legs by flexing hips and tilting pelvis backward until legs are parallel to floor or higher. Lower in controlled tempo.",
            proTips = "Focus on curling pelvis upward toward ribs at top to fully contract rectus abdominis rather than just lifting thighs.",
            commonMistakes = "Swinging legs for momentum (kipping), arching lower back at bottom, letting shoulders disengage.",
            videoUrl = "https://www.youtube.com/watch?v=hdng3Nm1x_E",
            youtubeVideoId = "hdng3Nm1x_E"
        ),
        ExerciseGuide(
            id = 12,
            name = "Face Pull",
            muscleGroup = "Shoulders",
            equipment = "Cable Machine & Rope",
            difficulty = "Beginner",
            setupInstructions = "Set cable pulley at upper chest/face level with rope attachment. Stand back with soft knees and grip rope thumbs-facing-back.",
            executionInstructions = "Pull rope toward nose/forehead, separating hands while rotating thumbs backward so knuckles end up beside ears.",
            proTips = "Incorporate external rotation at end of pull to strengthen rear deltoids and rotator cuff for shoulder health.",
            commonMistakes = "Pulling to chest instead of face, internal rotation of shoulders, using too much weight causing body swing.",
            videoUrl = "https://www.youtube.com/watch?v=rep-qVOkqgk",
            youtubeVideoId = "rep-qVOkqgk"
        )
    )

    fun getAllGuides(): List<ExerciseGuide> = exercises

    fun filterGuides(query: String? = null, muscleGroup: String? = null): List<ExerciseGuide> {
        return exercises.filter { item ->
            val matchesQuery = query.isNullOrBlank() || 
                item.name.contains(query, ignoreCase = true) ||
                item.muscleGroup.contains(query, ignoreCase = true) ||
                item.equipment.contains(query, ignoreCase = true)

            val matchesMuscle = muscleGroup.isNullOrBlank() || 
                muscleGroup.equals("All", ignoreCase = true) || 
                item.muscleGroup.equals(muscleGroup, ignoreCase = true)

            matchesQuery && matchesMuscle
        }
    }
}
