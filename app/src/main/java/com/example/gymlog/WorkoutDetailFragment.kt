package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
//import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.gymlog.databinding.FragmentWorkoutDetailBinding
import kotlin.getValue

class WorkoutDetailFragment : Fragment() {

    // 1. Set up View Binding for safe access to views
    private var _binding: FragmentWorkoutDetailBinding? = null
    private val binding get() = _binding!!

    // 2. Get the same ViewModel instance used by the Activity and other Fragments
    private val workoutViewModel: WorkoutViewModel by activityViewModels()

    // 3. Get the navigation arguments (the workoutId) safely
//    private val args: WorkoutDetailFragmentArgs by navArgs()

    // Variable to hold the workout being edited
    private var currentWorkout: Workout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        _binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 4. Use the workoutId from the arguments to get the specific workout
        //    The ViewModel returns LiveData, so we observe it for changes.
        workoutViewModel.getWorkoutById(this.id).observe(viewLifecycleOwner, Observer { workout ->
            // When the workout data is loaded, populate the UI
            workout?.let {
                currentWorkout = it
                populateUI(it)
            }
        })

        // 5. Set up the click listener for the "Save Changes" button
        binding.buttonSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    /**
     * Fills the EditText fields with the data from the loaded workout.
     */
    private fun populateUI(workout: Workout) {
        binding.editTextExerciseName.setText(workout.name)
        binding.editTextSets.setText(workout.sets.toString())
        binding.editTextReps.setText(workout.reps.toString())
        binding.editTextWeight.setText(workout.weight.toString())
    }

    /**
     * Reads data from UI, validates it, updates the database, and navigates back.
     */
    private fun saveChanges() {
        // Read the new values from the EditText fields
        val name = binding.editTextExerciseName.text.toString()
        val setsText = binding.editTextSets.text.toString()
        val repsText = binding.editTextReps.text.toString()
        val weightText = binding.editTextWeight.text.toString()

        // Basic validation
        if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()) {
            Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Update the 'currentWorkout' object with the new data
        currentWorkout?.let {
            it.name = name
            it.sets = setsText.toInt()
            it.reps = repsText.toInt()
            it.weight = weightText.toDouble()

            // Tell the ViewModel to update the workout in the database
            workoutViewModel.update(it)

            Toast.makeText(requireContext(), "Workout Updated", Toast.LENGTH_SHORT).show()

            // Navigate back to the previous screen (the workout list)
            findNavController().navigateUp()
        }
    }

    /**
     * Clean up the binding object when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
