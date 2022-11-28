package com.pixellore.checklist.DatabaseUtility

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table ORDER BY task_id ASC")
    fun getItems(): Flow<List<Task>>

    @Transaction
    @Query("SELECT * FROM task_table")
    fun getTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>

    /*@Query("SELECT * FROM action_item_table WHERE rowid IN (:itemIds)")
    fun getItemByIds(itemIds: IntArray): List<ActionItem>*/

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table")
    suspend fun deleteAll()


    @Insert
    suspend fun insertSubtask(subtask: Subtask)

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)
}