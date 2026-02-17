package com.example.gymlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WorkoutViewModelFactory (private val repository: WorkoutRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkoutViewModel(repository) as T
    }
}
//        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)){
//            @Suppress("UNCHECKED_CAST")
//       }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}