package com.pixellore.checklist.DatabaseUtility

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtask_table")
data class Subtask(
    @PrimaryKey val subtask_id:Int,
    var parent_task_id: Int,
    var subtask_title:String,
    var subtask_isCompleted: Boolean = false
) {
}