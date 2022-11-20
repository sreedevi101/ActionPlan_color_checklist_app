package com.pixellore.checklist.DatabaseUtility

import androidx.room.*


@Entity(tableName = "action_item_table")
data class Task(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "title") val title:String,
    @ColumnInfo(name = "details_note") val details_note:String = "",
    @ColumnInfo(name = "isImportant") val isImportant:Boolean = false,
    @ColumnInfo(name = "priority") val priority:String = "Low")