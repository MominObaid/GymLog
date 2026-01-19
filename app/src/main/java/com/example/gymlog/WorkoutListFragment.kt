package com.example.gymlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymlog.databinding.FragmentWorkoutListBinding

class WorkoutListFragment : Fragment() {
    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding

    private val workoutViewModel: WorkoutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkoutListBinding.inflate(inflater,container,false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WorkoutAdapter()
        binding?.recyclerViewWorkouts?.adapter = adapter
        binding?.recyclerViewWorkouts?.layoutManager = LinearLayoutManager(requireContext())

        workoutViewModel.allWorkouts.observe(viewLifecycleOwner, Observer{ workouts ->
            workouts.let{ adapter.setData(it)}  //could be error here
        })
        setupItemTouchHelper(adapter)
    }
    private fun setupItemTouchHelper(adapter: WorkoutAdapter){
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewModel: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean{
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction : Int){
                val position = viewHolder.adapterPosition
                val workoutToDelete.delete = adapter.getWorkoutAt(position)
                workoutViewModel.delete(workoutToDelete)
                Toast.makeText(requireContext(),"Workout Deleted!", Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding?.recyclerViewWorkouts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}