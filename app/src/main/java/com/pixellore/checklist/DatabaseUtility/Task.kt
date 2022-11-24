package com.pixellore.checklist.DatabaseUtility

import androidx.room.*


@Entity(tableName = "action_item_table")
data class Task(
    @PrimaryKey val id:Int,
    @ColumnInfo(name = "title") var title:String,
    @ColumnInfo(name = "details_note") var details_note:String = "",
    @ColumnInfo(name = "priority") var priority:String = "None")