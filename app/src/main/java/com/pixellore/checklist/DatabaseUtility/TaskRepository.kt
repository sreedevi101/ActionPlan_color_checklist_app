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

    fun allTasksWithSubtasksByChecklistId(checklistId: Int): Flow<List<TaskWithSubtasks>>{
        return actionItemDao.getTasksWithSubtasksByChecklistId(checklistId)
    }

    val allChecklists: Flow<List<Checklist>> = actionItemDao.getChecklists()

    val allChecklistsByPinned: Flow<List<Checklist>> = actionItemDao.getChecklistsByPinned()

    fun getAllTaskIds(): Flow<List<Int>> {
        return actionItemDao.getAllTaskIds()
    }

    fun getAllSubtaskIds(): Flow<List<Int>> {
        return actionItemDao.getAllSubtaskIds()
    }

    fun getAllChecklistIds(): Flow<List<Int>> {
        return actionItemDao.getAllChecklistIds()
    }

    fun getAllChecklistIdsSortedByPinned(): Flow<List<Int>> {
        return actionItemDao.getAllChecklistIdsSortedByPinned()
    }

    fun getSubtaskIdsByTaskId(taskId: Int): Flow<List<Int>>{
        return actionItemDao.getSubtaskIdsByTaskId(taskId)
    }

    fun getTaskIdsByChecklistId(checklistId: Int): Flow<List<Int>>{
        return actionItemDao.getTaskIdsByChecklistId(checklistId)
    }

    suspend fun getSubtaskById(subtaskId: Int): Subtask?{
        return actionItemDao.getSubtaskById(subtaskId)
    }

    suspend fun getTaskById(taskId: Int): Task?{
        return actionItemDao.getTaskById(taskId)
    }


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing Int running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread

    // Insert
    suspend fun insertTask(task: Task) {
        actionItemDao.insertTask(task)
    }

    suspend fun insertSubtask(subtask: Subtask) {
        actionItemDao.insertSubtask(subtask)
    }

    suspend fun insertChecklist(checklist: Checklist) {
        actionItemDao.insertChecklist(checklist)
    }

    // Update
    suspend fun updateTask(task: Task) {
        actionItemDao.updateTask(task)
    }

    suspend fun updateTaskId(oldId: Int, newId: Int){
        actionItemDao.updateTaskId(oldId, newId)
    }

    suspend fun updateTaskParentChecklistId(oldId: Int, oldParentChecklistId: Int, newParentChecklistId: Int){
        actionItemDao.updateTaskParentChecklistId(oldId, oldParentChecklistId, newParentChecklistId)
    }

    suspend fun updateSubtask(subtask: Subtask) {
        actionItemDao.updateSubtask(subtask)
    }

    suspend fun updateSubtaskId(oldId: Int, newId: Int){
        actionItemDao.updateSubtaskId(oldId, newId)
    }

    suspend fun updateSubtaskParentTaskId(oldId: Int, oldParentTaskId: Int, newParentTaskId: Int){
        actionItemDao.updateSubtaskParentTaskId(oldId, oldParentTaskId, newParentTaskId)
    }

    suspend fun updateChecklist(checklist: Checklist) {
        actionItemDao.updateChecklist(checklist)
    }

    suspend fun updateChecklistId(oldId: Int, newId: Int){
        actionItemDao.updateChecklistId(oldId, newId)
    }

    suspend fun updateChecklistOrder(uniqueId: Int, newPosId: Int){
        actionItemDao.updateChecklistOrder(uniqueId, newPosId)
    }

    suspend fun updateTaskOrder(uniqueId: Int, newPosId: Int){
        actionItemDao.updateTaskOrder(uniqueId, newPosId)
    }

    suspend fun updateSubtaskOrder(uniqueId: Int, newPosId: Int){
        actionItemDao.updateSubtaskOrder(uniqueId, newPosId)
    }

    // Delete
    suspend fun deleteTask(task: Task) {
        actionItemDao.deleteTask(task)
    }

    suspend fun deleteSubtask(subtask: Subtask) {
        actionItemDao.deleteSubtask(subtask)
    }

    suspend fun deleteChecklist(checklist: Checklist) {
        actionItemDao.deleteChecklist(checklist)
    }

    // Delete a list of items by their IDs
    suspend fun deleteTasks(taskIds: List<Int>) {
        actionItemDao.deleteTasks(taskIds)
    }

    suspend fun deleteSubtasks(subtaskIds: List<Int>){
        actionItemDao.deleteSubtasks(subtaskIds)
    }

    // Delete all
    suspend fun deleteAllTasks() {
        actionItemDao.deleteAllTasks()
    }

    suspend fun deleteAllSubtasks() {
        actionItemDao.deleteAllSubtasks()
    }

    suspend fun deleteAllChecklists() {
        actionItemDao.deleteAllChecklists()
    }
}