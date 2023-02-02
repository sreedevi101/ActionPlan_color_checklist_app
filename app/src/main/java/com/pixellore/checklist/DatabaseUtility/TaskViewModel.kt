package com.pixellore.checklist.DatabaseUtility

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ActionPlanViewModel(private val repository: TaskRepository) : ViewModel() {

    // Using LiveData and caching what 'getItems' returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allChecklistTasks: LiveData<List<Task>> = repository.allChecklistTasks.asLiveData()

    val allChecklistSubtasks: LiveData<List<Subtask>> = repository.allChecklistSubtasks.asLiveData()

    val allTasksWithSubtasks: LiveData<List<TaskWithSubtasks>> =
        repository.allTasksWithSubtasks.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(task: Task) = viewModelScope.launch() {
        repository.insert(task)
    }

    fun insertSubtask(subtask: Subtask) = viewModelScope.launch {
        repository.insertSubtask(subtask)
    }

    // Update
    fun update(task: Task) = viewModelScope.launch() {
        repository.update(task)
    }

    fun updateSubtask(subtask: Subtask) = viewModelScope.launch() {
        repository.updateSubtask(subtask)
    }

    // Delete
    fun delete(task: Task) = viewModelScope.launch() {
        repository.delete(task)
    }

    fun deleteSubtask(subtask: Subtask) = viewModelScope.launch() {
        repository.deleteSubtask(subtask)
    }

    // Delete All
    fun deleteAll() = viewModelScope.launch() {
        repository.deleteAll()
    }

    fun deleteAllSubtasks() = viewModelScope.launch() {
        repository.deleteAllSubtasks()
    }
}

class ActionPlanViewModelFactory(private val repository: TaskRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActionPlanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActionPlanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}