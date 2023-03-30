package com.pixellore.checklist.DatabaseUtility

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_table ORDER BY task_pos_id ASC")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM subtask_table ORDER BY subtask_pos_id ASC")
    fun getSubtasks(): Flow<List<Subtask>>

    @Transaction
    @Query("SELECT * FROM task_table ORDER BY task_pos_id ASC")
    fun getTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM task_table WHERE parent_checklist_id = :checklistId ORDER BY task_pos_id ASC")
    fun getTasksWithSubtasksByChecklistId(checklistId: Int): Flow<List<TaskWithSubtasks>>

    //----------------------Task----------------------------

    @Query("SELECT task_id FROM task_table ORDER BY task_id ASC")
    fun getAllTaskIds(): Flow<List<Int>>


    @Query("SELECT * FROM task_table WHERE task_id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT task_id FROM task_table WHERE parent_checklist_id = :checklistId ORDER BY task_id ASC")
    fun getTaskIdsByChecklistId(checklistId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTask(task: Task)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateTask(task: Task)

    @Query("UPDATE task_table SET task_id = :newId WHERE task_id = :oldId AND " +
            "NOT EXISTS (SELECT 1 FROM task_table WHERE task_id = :newId)")
    suspend fun updateTaskId(oldId: Int, newId: Int)

    @Query("UPDATE task_table SET parent_checklist_id = :newParentChecklistId WHERE " +
            "parent_checklist_id = :oldParentChecklistId AND task_id = :oldId")
    suspend fun updateTaskParentChecklistId(oldId: Int, oldParentChecklistId: Int, newParentChecklistId: Int)

    @Query("UPDATE task_table SET task_pos_id = :newPosId WHERE task_id = :uniqueId")
    suspend fun updateTaskOrder(uniqueId: Int, newPosId: Int)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM task_table WHERE task_id IN (:taskIds)")
    suspend fun deleteTasks(taskIds: List<Int>)


    @Query("DELETE FROM task_table")
    suspend fun deleteAllTasks()

    //----------------------Subtask----------------------------

    @Query("SELECT subtask_id FROM subtask_table ORDER BY subtask_id ASC")
    fun getAllSubtaskIds(): Flow<List<Int>>

    @Query("SELECT * FROM subtask_table WHERE subtask_id = :subtaskId")
    suspend fun getSubtaskById(subtaskId: Int): Subtask?

    @Query("SELECT subtask_id FROM subtask_table WHERE parent_task_id = :taskId ORDER BY subtask_id ASC")
    fun getSubtaskIdsByTaskId(taskId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubtask(subtask: Subtask)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateSubtask(subtask: Subtask)

    @Query("UPDATE subtask_table SET subtask_id = :newId WHERE subtask_id = :oldId AND " +
            "NOT EXISTS (SELECT 1 FROM subtask_table WHERE subtask_id = :newId)")
    suspend fun updateSubtaskId(oldId: Int, newId: Int)

    @Query("UPDATE subtask_table SET parent_task_id = :newParentTaskId WHERE " +
            "parent_task_id = :oldParentTaskId AND subtask_id = :oldId")
    suspend fun updateSubtaskParentTaskId(oldId: Int, oldParentTaskId: Int, newParentTaskId: Int)


    @Query("UPDATE subtask_table SET subtask_pos_id = :newPosId WHERE subtask_id = :uniqueId")
    suspend fun updateSubtaskOrder(uniqueId: Int, newPosId: Int)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("DELETE FROM subtask_table WHERE subtask_id IN (:subtaskIds)")
    suspend fun deleteSubtasks(subtaskIds: List<Int>)

    @Query("DELETE FROM subtask_table")
    suspend fun deleteAllSubtasks()

    //----------------------Checklist----------------------------

    @Query("SELECT * FROM checklist_table ORDER BY checklist_id ASC")
    fun getChecklists(): Flow<List<Checklist>>

    @Query("SELECT checklist_id FROM checklist_table ORDER BY checklist_id ASC")
    fun getAllChecklistIds(): Flow<List<Int>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChecklist(checklist: Checklist)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateChecklist(checklist: Checklist)

    @Query("UPDATE checklist_table SET checklist_id = :newId WHERE checklist_id = :oldId  AND" +
            " NOT EXISTS (SELECT 1 FROM subtask_table WHERE subtask_id = :newId)")
    suspend fun updateChecklistId(oldId: Int, newId: Int)


    @Query("UPDATE checklist_table SET checklist_pos_id = :newPosId WHERE checklist_id = :uniqueId")
    suspend fun updateChecklistOrder(uniqueId: Int, newPosId: Int)

    @Delete
    suspend fun deleteChecklist(checklist: Checklist)

    @Query("DELETE FROM checklist_table")
    suspend fun deleteAllChecklists()
}