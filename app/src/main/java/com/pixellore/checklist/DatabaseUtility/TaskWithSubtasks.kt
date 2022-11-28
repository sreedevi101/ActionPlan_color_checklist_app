package com.pixellore.checklist.DatabaseUtility

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubtasks(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "task_id",
        entityColumn = "parent_task_id"
    )
    val subtaskList: List<Subtask>
) {
}