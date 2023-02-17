package com.pixellore.checklist.DatabaseUtility

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table ORDER BY task_id ASC")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM subtask_table ORDER BY subtask_id ASC")
    fun getSubtasks(): Flow<List<Subtask>>

    @Transaction
    @Query("SELECT * FROM task_table")
    fun getTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM task_table WHERE parent_checklist_id = :checklistId ORDER BY task_id ASC")
    fun getTasksWithSubtasksByChecklistId(checklistId: Int): Flow<List<TaskWithSubtasks>>

    /*@Query("SELECT * FROM action_item_table WHERE rowid IN (:itemIds)")
    fun getItemByIds(itemIds: IntArray): List<ActionItem>*/

    @Query("SELECT * FROM checklist_table ORDER BY checklist_id ASC")
    fun getChecklists(): Flow<List<Checklist>>

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

    @Query("DELETE FROM subtask_table")
    suspend fun deleteAllSubtasks()



    @Insert
    suspend fun insertChecklist(checklist: Checklist)

    @Update
    suspend fun updateChecklist(checklist: Checklist)

    @Delete
    suspend fun deleteChecklist(checklist: Checklist)

    @Query("DELETE FROM checklist_table")
    suspend fun deleteAllChecklists()
}