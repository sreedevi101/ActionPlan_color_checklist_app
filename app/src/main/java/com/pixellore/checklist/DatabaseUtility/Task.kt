package com.pixellore.checklist.DatabaseUtility

import androidx.room.*


@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey val task_id:Int,
    var task_title:String,
    var details_note:String = "",
    var due_date:String = "",
    var priority:String = "None",
    var isExpanded: Boolean = false
)