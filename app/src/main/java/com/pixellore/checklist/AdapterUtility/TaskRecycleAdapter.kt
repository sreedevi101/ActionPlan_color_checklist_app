package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.TaskWithSubtasks
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.Constants
import com.pixellore.checklist.utils.SpaceDecorator

/**
 * Adapter for the RecyclerView to display task items (in database) as a list
 *
 * */

class TaskRecycleAdapter(
    private val clickListener: (position: Int, taskWithSubtasks: TaskWithSubtasks, actionRequested: Int) -> Unit,
    private val clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit
) :
    ListAdapter<TaskWithSubtasks, TaskRecycleAdapter.TaskRecycleViewHolder>(ActionItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskRecycleViewHolder {
        return TaskRecycleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TaskRecycleViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.bind(currentTask, holder.itemView.context, clickListener, clickListenerSubtask)
    }


    class TaskRecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val TAG = "Debug"


        fun bind(
            currentTask: TaskWithSubtasks, context: Context,
            clickListener: (position: Int, taskWithSubtasks: TaskWithSubtasks, actionRequested: Int) -> Unit,
            clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit
        ) {

            with(itemView) {
                val subtaskRecyclerView: RecyclerView = findViewById(R.id.subTaskRecycler)
                var subtaskRecyclerAdapter = SubTaskRecycleAdapter(clickListenerSubtask)

                val taskTitleView: TextView = findViewById(R.id.actionItemTitle)
                val detailsNote: TextView = findViewById(R.id.detailsNote)
                val taskDueDate: TextView = findViewById(R.id.dueDate)


                val subLayout: ConstraintLayout = findViewById(R.id.subLayout)
                val taskCardLayout: ConstraintLayout = findViewById(R.id.taskCardLayout)

                val expandCollapseBtn: ImageButton =
                    findViewById(R.id.expandCollapseSubLayoutButton)

                val completedCheckBox: CheckBox = findViewById(R.id.taskCompletedCheck)

                taskTitleView.text = currentTask.task.task_title
                taskDueDate.text = currentTask.task.due_date
                detailsNote.text = currentTask.task.details_note

                // bind view according to the status of  isExpanded (Default: false)
                toggleSubtasksDisplay(currentTask, subLayout)

                // bind CheckBox status according to the value of the state task_isCompleted (Default: false)
                toggleStrikeThrough(
                    textViewToStrike = taskTitleView,
                    currentTask.task.task_isCompleted
                )
                completedCheckBox.isChecked = currentTask.task.task_isCompleted


                // Subtask list RecyclerView
                subtaskRecyclerAdapter.submitList(currentTask.subtaskList)
                subtaskRecyclerView.addItemDecoration(SpaceDecorator(0))
                subtaskRecyclerView.adapter = subtaskRecyclerAdapter
                subtaskRecyclerView.layoutManager = LinearLayoutManager(context)

                /*
                * Expand and Collapse (Toggle) the subLayout on pressing the down/up arrow button
                * */
                expandCollapseBtn.setOnClickListener {
                    //currentTask.subtaskList.forEach { Log.v(TAG, it.subtask_title) }

                    currentTask.task.isExpanded = !currentTask.task.isExpanded
                    toggleSubtasksDisplay(currentTask, subLayout)
                    // Update in database
                    clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)
                }


                /*
                * Check the checkbox and strike through the text on pressing the checkbox
                * */
                completedCheckBox.setOnClickListener {
                    val isChecked = completedCheckBox.isChecked
                    currentTask.task.task_isCompleted = isChecked
                    toggleStrikeThrough(textViewToStrike = taskTitleView, isChecked)
                    // Update in database
                    clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)
                }

                /**
                 * Open the item for editing
                 * */
                taskCardLayout.setOnClickListener {
                    clickListener(adapterPosition, currentTask, Constants.OPEN_EDITOR)
                }

            }

        }

        companion object {
            fun create(parent: ViewGroup): TaskRecycleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_item, parent, false)
                return TaskRecycleViewHolder((view))
            }
        }


        private fun toggleStrikeThrough(textViewToStrike: TextView, isChecked: Boolean) {
            if (isChecked) {
                textViewToStrike.paintFlags =
                    textViewToStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textViewToStrike.paintFlags =
                    textViewToStrike.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }


        private fun toggleSubtasksDisplay(currentTask: TaskWithSubtasks, view: ConstraintLayout) {
            //Debug
            val ex = currentTask.task.isExpanded
            Log.v(TAG, "isExpanded: $ex")
            //
            if (currentTask.task.isExpanded) {
                // isExpanded: true -> Expand
                Log.v(TAG, "Expand")
                view.visibility = View.VISIBLE
            } else {
                // isExpanded: false -> Collapse
                Log.v(TAG, "Collapse")
                view.visibility = View.GONE
            }
        }


    }


    /*
    * Callback for calculating the diff between two non-null items in a list.
    * The WordsComparator defines how to compute if two words are the same or if the contents are the same.
    * */
    class ActionItemComparator : DiffUtil.ItemCallback<TaskWithSubtasks>() {
        override fun areItemsTheSame(
            oldItem: TaskWithSubtasks,
            newItem: TaskWithSubtasks
        ): Boolean {
            /*     Log.v("Debug", "areItemsTheSame:\nold: " + oldItem.task.task_id +
                         " new: " + newItem.task.task_id +
                         " comparison: " + (oldItem.task.task_id == newItem.task.task_id))*/
            return oldItem.task.task_id == newItem.task.task_id
        }

        override fun areContentsTheSame(
            oldItem: TaskWithSubtasks,
            newItem: TaskWithSubtasks
        ): Boolean {
            /*Log.v("Debug", "areContentsTheSame:\n" + "item:" + oldItem.task.task_title +
                    "\nold: " + oldItem.task.task_isCompleted +
                    " \nnew: " + newItem.task.task_isCompleted +
                    " \ncomparison: " + (oldItem == newItem))*/
            return oldItem == newItem
        }
    }


}