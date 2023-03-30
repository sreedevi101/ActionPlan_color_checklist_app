package com.pixellore.checklist.DatabaseUtility

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow

class ActionPlanViewModel(private val repository: TaskRepository) : ViewModel() {

    // Using LiveData and caching what 'getItems' returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allChecklistTasks: LiveData<List<Task>> = repository.allChecklistTasks.asLiveData()

    val allChecklistSubtasks: LiveData<List<Subtask>> = repository.allChecklistSubtasks.asLiveData()

    val allTasksWithSubtasks: LiveData<List<TaskWithSubtasks>> =
        repository.allTasksWithSubtasks.asLiveData()

    fun allTasksWithSubtasksByChecklistId(checklistId: Int): LiveData<List<TaskWithSubtasks>> {
        return repository.allTasksWithSubtasksByChecklistId(checklistId).asLiveData()
    }

    val allChecklists: LiveData<List<Checklist>> = repository.allChecklists.asLiveData()

    fun getAllTaskIds(): LiveData<List<Int>> {
        return repository.getAllTaskIds().asLiveData()
    }

    fun getAllSubtaskIds(): LiveData<List<Int>> {
        return repository.getAllSubtaskIds().asLiveData()
    }

    fun getAllChecklistIds(): LiveData<List<Int>> {
        return repository.getAllChecklistIds().asLiveData()
    }

    fun getTaskIdsByChecklistId(checklistId: Int): Flow<List<Int>> {
        return repository.getTaskIdsByChecklistId(checklistId)
    }

    suspend fun getSubtaskById(subtaskId: Int): Subtask?{
        return repository.getSubtaskById(subtaskId)
    }

    suspend fun getTaskById(taskId: Int): Task?{
        return repository.getTaskById(taskId)
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertTask(task: Task) = viewModelScope.launch() {
        repository.insertTask(task)
    }

    fun insertSubtask(subtask: Subtask) = viewModelScope.launch {
        repository.insertSubtask(subtask)
    }

    fun insertChecklist(checklist: Checklist) = viewModelScope.launch {
        repository.insertChecklist(checklist)
    }

    // Update
    fun updateTask(task: Task) = viewModelScope.launch() {
        repository.updateTask(task)
    }

    fun updateSubtask(subtask: Subtask) = viewModelScope.launch() {
        repository.updateSubtask(subtask)
    }

    fun updateChecklist(checklist: Checklist) = viewModelScope.launch() {
        repository.updateChecklist(checklist)
    }


    fun updateTaskId(oldId: Int, newId: Int) = viewModelScope.launch(){
        repository.updateTaskId(oldId, newId)
    }


    fun updateSubtaskId(oldId: Int, newId: Int) = viewModelScope.launch() {
        repository.updateSubtaskId(oldId, newId)
    }


    fun updateChecklistId(oldId: Int, newId: Int) = viewModelScope.launch(){
        repository.updateChecklistId(oldId, newId)
    }

    fun updateTaskParentChecklistId(oldId: Int,
                                    oldParentChecklistId: Int,
                                    newParentChecklistId: Int) = viewModelScope.launch(){
        repository.updateTaskParentChecklistId(oldId, oldParentChecklistId, newParentChecklistId)
    }

    fun updateSubtaskParentTaskId(oldId: Int,
                                  oldParentTaskId: Int,
                                  newParentTaskId: Int) = viewModelScope.launch(){
        repository.updateSubtaskParentTaskId(oldId, oldParentTaskId, newParentTaskId)
    }


    fun updateChecklistOrder(uniqueId: Int, newPosId: Int) = viewModelScope.launch(){
        repository.updateChecklistOrder(uniqueId, newPosId)
    }

    fun updateTaskOrder(uniqueId: Int, newPosId: Int) = viewModelScope.launch(){
        repository.updateTaskOrder(uniqueId, newPosId)
    }

    fun updateSubtaskOrder(uniqueId: Int, newPosId: Int) = viewModelScope.launch(){
        repository.updateSubtaskOrder(uniqueId, newPosId)
    }



    // Delete
    fun deleteTask(task: Task) = viewModelScope.launch() {
        repository.deleteTask(task)
    }

    fun deleteSubtask(subtask: Subtask) = viewModelScope.launch() {
        repository.deleteSubtask(subtask)
    }

    fun deleteChecklist(checklist: Checklist) = viewModelScope.launch() {
        repository.deleteChecklist(checklist)
    }


    // Delete a list of items by their IDs
    fun deleteTasks(taskIds: List<Int>) = viewModelScope.launch(){
        repository.deleteTasks(taskIds)
    }

    fun deleteSubtasks(subtaskIds: List<Int>) = viewModelScope.launch {
        repository.deleteSubtasks(subtaskIds)
    }

    // Delete All
    fun deleteAllTasks() = viewModelScope.launch() {
        repository.deleteAllTasks()
    }

    fun deleteAllSubtasks() = viewModelScope.launch() {
        repository.deleteAllSubtasks()
    }

    fun deleteAllChecklists() = viewModelScope.launch() {
        repository.deleteAllChecklists()
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