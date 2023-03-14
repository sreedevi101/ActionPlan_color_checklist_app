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
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.util.setVisibility
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.TaskWithSubtasks
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.Constants
import com.pixellore.checklist.utils.SpaceDecorator

/**
 * Adapter for the RecyclerView in ChecklistActivity
 * to display task items (in database) as a list
 *
 * */

class TaskRecycleAdapter(
    private val clickListener: (position: Int, taskWithSubtasks: TaskWithSubtasks, actionRequested: Int) -> Unit,
    private val clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit
) :
    ListAdapter<TaskWithSubtasks, TaskRecycleAdapter.TaskRecycleViewHolder>(ActionItemComparator()){

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
                var subtaskRecyclerAdapter = SubTaskRecycleAdapter(clickListenerSubtask,
                    { position, subtask -> onListSubtaskLayoutClick(position, subtask) })

                val taskTitleView: TextView = findViewById(R.id.actionItemTitle)
                val detailsNote: TextView = findViewById(R.id.detailsNote)
                val taskDueDate: TextView = findViewById(R.id.dueDate)


                val subLayout: ConstraintLayout = findViewById(R.id.subLayout)
                val taskCardLayout: ConstraintLayout = findViewById(R.id.taskCardLayout)

                val expandCollapseBtn: ImageButton = findViewById(R.id.expand_collapse_button)
                val priorityBtn: ImageButton = findViewById(R.id.priority_button)
                val moreOptionsBtn: ImageButton = findViewById(R.id.more_options)

                val completedCheckBox: CheckBox = findViewById(R.id.taskCompletedCheck)

                // Title
                taskTitleView.text = currentTask.task.task_title
                // Due date
                if (!currentTask.task.due_date.equals("")){
                    taskDueDate.text = currentTask.task.due_date
                    taskDueDate.visibility = View.VISIBLE
                } else {
                    taskDueDate.visibility = View.GONE
                }
                // Details
                if (!currentTask.task.details_note.equals("")){
                    detailsNote.text = currentTask.task.details_note
                    detailsNote.visibility = View.VISIBLE
                } else {
                    detailsNote.visibility = View.GONE
                }

                /*
                * Display expand-collapse button only if there is any task details or subtasks to display
                * */
                if (currentTask.subtaskList.isEmpty() && currentTask.task.details_note.equals("")){
                    expandCollapseBtn.visibility = View.GONE
                } else {
                    expandCollapseBtn.visibility = View.VISIBLE
                }


                // bind view according to the status of  isExpanded (Default: false)
                toggleSubtasksDisplay(currentTask, subLayout)
                // set icon according to the status of expand-collapse
                toggleExpandCollapseIconDisplay(currentTask, expandCollapseBtn)

                // Set priority Icon according to the status of the priority field
                setPriorityIcon(currentTask, priorityBtn)

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

                    // toggle visibility of the subtasks
                    toggleSubtasksDisplay(currentTask, subLayout)

                    // Toggle the expand-collapse icon with drop-down and drop-up
                    toggleExpandCollapseIconDisplay(currentTask, expandCollapseBtn)
                    // Update in database
                    clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)
                }

                /*
                * Change priority each time the button is clicked
                *
                * Priority moves from "None" (default) to "Medium" -> "High" -> back to "None"
                * */
                priorityBtn.setOnClickListener {
                    currentTask.task.priority = when(currentTask.task.priority) {
                        "None" -> "Medium"
                        "Medium" -> "High"
                        "High" -> "None"
                        else -> {
                            // Show error message
                            Toast.makeText(context,
                            "Error in setting Priority", Toast.LENGTH_SHORT).show()

                            // keep the current value of priority field
                            currentTask.task.priority
                        }
                    }
                    setPriorityIcon(currentTask, priorityBtn)
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



                moreOptionsBtn.setOnClickListener {
                    val moreEditMenu = PopupMenu(context, moreOptionsBtn)
                    val menu = moreEditMenu.menu
                    moreEditMenu.menuInflater.inflate(R.menu.checklist_item_popup_menu, menu)
                    moreEditMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.popup_delete_checklist_item -> {
                                // Handle edit action
                                clickListener(adapterPosition, currentTask, Constants.DELETE)
                                true
                            }
                            else -> false
                        }
                    }
                    moreEditMenu.show()
                }
            }
        }


        private fun onListSubtaskLayoutClick(position: Int, subtask: Subtask) {
            //clickListener(adapterPosition, currentTask, Constants.OPEN_EDITOR)
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

        /*
        * Change icon displayed based on the 'currentTask' field 'priority'
        * */
        private fun setPriorityIcon(currentTask: TaskWithSubtasks, priorityButton: ImageButton) {
            when(currentTask.task.priority){
                "None" -> priorityButton.setImageResource(R.drawable.star_outline)
                "Medium" -> priorityButton.setImageResource(R.drawable.star_half)
                "High" -> priorityButton.setImageResource(R.drawable.star_filled)
            }
        }

        /*
        * set visibility of view (input) based on the 'currentTask' field 'isExpanded'
        * */
        private fun toggleSubtasksDisplay(currentTask: TaskWithSubtasks, view: ConstraintLayout) {
            val ex = currentTask.task.isExpanded

            if (currentTask.task.isExpanded) {
                // isExpanded: true -> Expand
                view.visibility = View.VISIBLE
            } else {
                // isExpanded: false -> Collapse
                view.visibility = View.GONE
            }
        }

        /*
        * Change icon displayed based on the 'currentTask' field 'isExpanded'
        * */
        private fun toggleExpandCollapseIconDisplay(currentTask: TaskWithSubtasks, buttonImage: ImageButton) {
            val ex = currentTask.task.isExpanded

            if (currentTask.task.isExpanded) {
                // isExpanded: true -> Expand
                buttonImage.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
            } else {
                // isExpanded: false -> Collapse
                buttonImage.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
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