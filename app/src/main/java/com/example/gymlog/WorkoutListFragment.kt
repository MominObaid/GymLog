//package com.example.gymlog
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.gymlog.databinding.FragmentWorkoutListBinding
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class WorkoutListFragment : Fragment() {
//    private var _binding: FragmentWorkoutListBinding? = null
//    private val binding get() = _binding!!
//
//    private val workoutViewModel: WorkoutViewModel by activityViewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentWorkoutListBinding.inflate(inflater,container,false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val adapter = WorkoutAdapter()
//        binding.recyclerViewWorkouts.adapter = adapter
//        binding.recyclerViewWorkouts.layoutManager = LinearLayoutManager(requireContext())
//
//        //Observe the data from the ViewModel
//        workoutViewModel.allWorkouts.observe(viewLifecycleOwner, Observer { workouts ->
//            //we use ?.let for safety, although observer should not fire with null
//            workouts?.let { adapter.setData(it) }  //could be error here
//        })
//        //setup Swip-to-delete
//        setupItemTouchHelper(adapter)
//        //FIXED: Navigation logic is now inside the click listener where it belongs
//        adapter.setOnItemClickListener { workout ->
//            val action =WorkoutListFragmentDirections.actionWorkoutListFragmentToWorkoutDetailFragment()
//            findNavController().navigate(action)
//        }
//        binding.buttonAddExercise.setOnClickListener {
//            addWorkout()
//        }
//    }
//    private fun setupItemTouchHelper(adapter: WorkoutAdapter){
//        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
//            0,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewModel: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean{
//                return false
//            }
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction : Int){
//                val position = viewHolder.adapterPosition
//                val workoutToDelete = adapter.getWorkoutAt(position)
//                workoutViewModel.delete(workoutToDelete)
//                Toast.makeText(requireContext(),"Workout Deleted!", Toast.LENGTH_SHORT).show()
//            }
//        }
//        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
//        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWorkouts)
//    }
//
//    fun addWorkout(){
//        val name = binding.editTextExerciseName.text.toString()
//        val setsText =binding.editTextSets.text.toString()
//        val repsText = binding.editTextReps.text.toString()
//        val weightText = binding.editTextWeight.text.toString()
//        if (name.isBlank() || setsText.isBlank() || repsText.isBlank() || weightText.isBlank()){
//            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
//            return
//        }
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        val workout = Workout(
//            name = name,
//            sets = setsText.toInt(),
//            reps = repsText.toInt(),
//            weight = weightText.toDouble(),
//            date = currentDate
//        )
//        workoutViewModel.insert(workout)
//        Toast.makeText(requireContext(),"Exercise Logged!", Toast.LENGTH_SHORT).show()
//        findNavController().navigateUp()
//        clearInputFields()
//    }
//    private fun clearInputFields(){
//        binding.editTextExerciseName.text.clear()
//        binding.editTextSets.text.clear()
//        binding.editTextReps.text.clear()
//        binding.editTextWeight.text.clear()
//        binding.editTextExerciseName.requestFocus()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}