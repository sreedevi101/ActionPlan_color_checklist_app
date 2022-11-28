package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.MainActivity
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.SpaceDecorator

/**
* Adapter for the RecyclerView to display task items (in database) as a list
 *
* */
class TaskRecycleAdapter:
    ListAdapter<TaskWithSubtasks, TaskRecycleAdapter.TaskRecycleViewHolder>(ActionItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskRecycleViewHolder {
        return TaskRecycleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskRecycleViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind2(current, holder.itemView.context)
    }


    class TaskRecycleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val TAG = "Debug"

        private val subtaskRecyclerView: RecyclerView = itemView.findViewById(R.id.subTaskRecycler)
        private var subtaskRecyclerAdapter = SubTaskRecycleAdapter()

        private val taskTitleView: TextView = itemView.findViewById(R.id.actionItemTitle)
        private val detailsNote:TextView = itemView.findViewById(R.id.detailsNote)
        private val taskDueDate: TextView = itemView.findViewById(R.id.dueDate)


        private val subLayout: ConstraintLayout = itemView.findViewById(R.id.subLayout)
        private val taskCardLayout: ConstraintLayout = itemView.findViewById(R.id.taskCardLayout)
        private val mainLayout: ConstraintLayout = itemView.findViewById(R.id.mainLayout)

        fun bind2(currentTask: TaskWithSubtasks, context: Context){
            taskTitleView.text = currentTask.task.task_title
            taskDueDate.text = currentTask.task.due_date
            detailsNote.text = currentTask.task.details_note

            // bind view according to the status of  isExpanded (Default: false)
            toggleSubtasksDisplay(currentTask)


            // Subtask list RecyclerView
            subtaskRecyclerAdapter.submitList(currentTask.subtaskList)
            subtaskRecyclerView.addItemDecoration(SpaceDecorator(0))
            subtaskRecyclerView.adapter = subtaskRecyclerAdapter
            subtaskRecyclerView.layoutManager = LinearLayoutManager(context)


            taskCardLayout.setOnClickListener {
                currentTask.subtaskList.forEach { Log.v(TAG, it.subtask_title) }

                currentTask.task.isExpanded = !currentTask.task.isExpanded
                // TODO: Update in database

                toggleSubtasksDisplay(currentTask)
            }
        }


        private fun toggleSubtasksDisplay(currentTask: TaskWithSubtasks) {
            //Debug
            val ex = currentTask.task.isExpanded
            Log.v(TAG, "isExpanded: $ex")
            //


            if (currentTask.task.isExpanded) {
                // isExpanded: true -> Expand
                Log.v(TAG, "Expand")
                subLayout.visibility = View.VISIBLE
            } else {
                // isExpanded: false -> Collapse
                Log.v(TAG, "Collapse")
                subLayout.visibility = View.GONE
            }
        }


        companion object {
            fun create(parent: ViewGroup): TaskRecycleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_item, parent, false)
                return TaskRecycleViewHolder((view))
            }
        }


    }
    /*
    * Callback for calculating the diff between two non-null items in a list.
    * The WordsComparator defines how to compute if two words are the same or if the contents are the same.
    * */
    class ActionItemComparator: DiffUtil.ItemCallback<TaskWithSubtasks>() {
        override fun areItemsTheSame(oldItem: TaskWithSubtasks, newItem: TaskWithSubtasks): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TaskWithSubtasks, newItem: TaskWithSubtasks): Boolean {
            return oldItem.task.task_title == newItem.task.task_title
        }
    }

}