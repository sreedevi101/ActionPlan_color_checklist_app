package com.pixellore.checklist.DatabaseUtility

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM action_item_table ORDER BY id ASC")
    fun getItems(): Flow<List<Task>>

    /*@Query("SELECT * FROM action_item_table WHERE rowid IN (:itemIds)")
    fun getItemByIds(itemIds: IntArray): List<ActionItem>*/

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM action_item_table")
    suspend fun deleteAll()
}