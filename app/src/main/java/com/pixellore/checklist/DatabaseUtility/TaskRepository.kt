package com.pixellore.checklist.DatabaseUtility

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

/*
* Repositories are meant to mediate between different data sources.
* A Repository manages queries and allows you to use multiple backends.
* A repository class abstracts access to multiple data sources.
* */

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TaskRepository(private val actionItemDao: TaskDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    val allChecklistTasks: Flow<List<Task>> = actionItemDao.getTasks()

    val allChecklistSubtasks: Flow<List<Subtask>> = actionItemDao.getSubtasks()

    val allTasksWithSubtasks: Flow<List<TaskWithSubtasks>> = actionItemDao.getTasksWithSubtasks()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread

    // Insert
    suspend fun insert(task: Task){
        actionItemDao.insert(task)
    }

    suspend fun insertSubtask(subtask: Subtask){
        actionItemDao.insertSubtask(subtask)
    }

    // Update
    suspend fun update(task: Task){
        actionItemDao.update(task)
    }

    suspend fun updateSubtask(subtask: Subtask){
        actionItemDao.updateSubtask(subtask)
    }

    // Delete
    suspend fun delete(task: Task){
        actionItemDao.delete(task)
    }

    suspend fun deleteSubtask(subtask: Subtask){
        actionItemDao.deleteSubtask(subtask)
    }

    // Delete all
    suspend fun deleteAll(){
        actionItemDao.deleteAll()
    }

    suspend fun deleteAllSubtasks(){
        actionItemDao.deleteAllSubtasks()
    }
}