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

    val allChecklistItems: Flow<List<Task>> = actionItemDao.getItems()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread

    suspend fun insert(task: Task){
        actionItemDao.insert(task)
    }
}