package com.pixellore.checklist.DatabaseUtility

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ActionPlanViewModel(private val repository: TaskRepository): ViewModel() {

    // Using LiveData and caching what 'getItems' returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allChecklistItems: LiveData<List<Task>> = repository.allChecklistItems.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(task: Task) = viewModelScope.launch(){
        repository.insert(task)
    }


}

class ActionPlanViewModelFactory(private val repository: TaskRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActionPlanViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ActionPlanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}