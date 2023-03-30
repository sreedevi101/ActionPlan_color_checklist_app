package com.pixellore.checklist.AdapterUtility

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.*
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.TaskWithSubtasks
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.*

/**
 * Adapter for the RecyclerView in ChecklistActivity
 * to display task items (in database) as a list
 *
 * activity: BaseActivity - to call BaseActivity functions from Adapter or ViewHolder class
 * */

class TaskRecycleAdapter(
    private val clickListener: (position: Int, taskWithSubtasks: TaskWithSubtasks, actionRequested: Int) -> Unit,
    private val clickListenerSubtask: (position: Int, subtask: Subtask) -> Unit,
    private val activity: BaseActivity
) :
    ListAdapter<TaskWithSubtasks, TaskRecycleAdapter.TaskRecycleViewHolder>(ActionItemComparator()), ItemTouchHelperAdapter{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskRecycleViewHolder {
        return TaskRecycleViewHolder.create(parent, activity)
    }

    override fun onBindViewHolder(holder: TaskRecycleViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.bind(currentTask, position,
            holder.itemView.context, clickListener, clickListenerSubtask)
    }


    override fun onItemMove(fromPosition: Int, toPosition: Int) {

        val currentTask = getItem(fromPosition)
        Log.v(Constants.TAG, "currentTask ${currentTask.task.task_title}, " +
                "current position ${currentTask.task.task_pos_id}")
        Log.v(Constants.TAG, "onItemMove ${fromPosition+1} to ${toPosition+1}")
        Log.v(Constants.TAG, "toPosition $toPosition")
        //currentTask.task.task_pos_id = toPosition

        // Update in database
        // move currentTask to toPosition, rearrange rest of the tasks in the checklist
       clickListener(toPosition+1, currentTask, Constants.REARRANGE)

        //notifyDataSetChanged()

    }

    class TaskRecycleViewHolder(itemView: View, activity: BaseActivity) :
        RecyclerView.ViewHolder(itemView),
        ItemTouchHelperViewHolder,
        View.OnTouchListener{

        init {
            itemView.setOnTouchListener(this)
        }


        val baseActivity: BaseActivity = activity

        fun bind(
            currentTask: TaskWithSubtasks, position: Int, context: Context,
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

                // set background color
                currentTask.task.task_font?.backgroundColorResId?.let {
                    taskCardLayout.setBackgroundColor(it)
                }

                // Title
                taskTitleView.text = currentTask.task.task_title
                // set title text color
                currentTask.task.task_font?.headingTextColorResId?.let {
                    taskTitleView.setTextColor(it)
                }
                // Due date
                if (!currentTask.task.due_date.equals("")){
                    val dueDateStatus = currentTask.task.due_date?.let {
                        baseActivity.compareDatesDisplay(it, 5)
                    }

                    taskDueDate.text = dueDateStatus
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

                // set color if bodyTextColorResId is not null
                currentTask.task.task_font?.bodyTextColorResId?.let {
                    Log.v(Constants.TAG, "bodyTextColorResId: $it")
                    detailsNote.setTextColor(it)
                    taskDueDate.setTextColor(it)

                    priorityBtn.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                    expandCollapseBtn.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
                    moreOptionsBtn.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)

                    val colorStateList = ColorStateList.valueOf(it)
                    completedCheckBox.buttonTintList = colorStateList
                }


                // set font if textFontName is not null
                currentTask.task.task_font?.textFontName?.let {
                    // Load the custom font file from the assets folder
                    val typeface = Typeface.createFromAsset(context.assets, it)
                    taskTitleView.typeface = typeface
                    detailsNote.typeface = typeface
                    taskDueDate.typeface = typeface
                }


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
                    moreEditMenu.menuInflater.inflate(R.menu.task_item_popup_menu, menu)
                    MenuCompat.setGroupDividerEnabled(menu, true);
                    moreEditMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.popup_change_background ->
                            {
                                ColorPickerDialog
                                    .Builder(context)        				// Pass Activity Instance
                                    .setTitle("Choose Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        // Handle Color Selection
                                        //Log.v(Constants.TAG, "Color Selected: $color")

                                        // change background color of task item
                                        taskCardLayout.setBackgroundColor(color)

                                        // modify task item to save in the database
                                        if (currentTask.task.task_font != null){
                                            currentTask.task.task_font?.backgroundColorResId = color
                                        } else{
                                            val font = CustomStyle(backgroundColorResId = color)
                                            currentTask.task.task_font = font
                                        }

                                        // update in database
                                        clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)

                                    }.show()

                                return@setOnMenuItemClickListener true
                            }
                            R.id.popup_change_title_color ->
                            {
                                ColorPickerDialog
                                    .Builder(context)        				// Pass Activity Instance
                                    .setTitle("Choose Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        // Handle Color Selection
                                        //Log.v(Constants.TAG, "Color Selected: $color")

                                        // change text color
                                        taskTitleView.setTextColor(color)

                                        // modify task item to save in the database
                                        if (currentTask.task.task_font != null){
                                            currentTask.task.task_font?.headingTextColorResId = color
                                        } else{
                                            val font = CustomStyle(headingTextColorResId = color)
                                            currentTask.task.task_font = font
                                        }

                                        // update in database
                                        clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)

                                    }.show()

                                return@setOnMenuItemClickListener true
                            }
                            R.id.popup_change_other_title_color ->
                            {
                                ColorPickerDialog
                                    .Builder(context)        				// Pass Activity Instance
                                    .setTitle("Choose Color")           	// Default "Choose Color"
                                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                                    .setColorListener { color, colorHex ->
                                        // Handle Color Selection
                                        Log.v(Constants.TAG, "Color Selected: $color")

                                        // modify task item to save in the database
                                        if (currentTask.task.task_font != null){
                                            currentTask.task.task_font?.bodyTextColorResId = color
                                        } else{
                                            val font = CustomStyle(bodyTextColorResId = color)
                                            currentTask.task.task_font = font
                                        }
                                        Log.v(Constants.TAG, "font: ${currentTask.task.task_font}")

                                        currentTask.subtaskList.forEach {
                                            if (it.subtask_font != null){
                                                it.subtask_font?.bodyTextColorResId = color
                                            } else{
                                                val font = CustomStyle(bodyTextColorResId = color)
                                                it.subtask_font = font
                                            }
                                        }

                                        // update in database
                                        clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)

                                        clickListener(adapterPosition, currentTask, Constants.UPDATE_DB_PLUS)

                                        // notify adapter to redraw this task item`
                                        val adapter = (itemView.parent as RecyclerView).adapter
                                        adapter?.notifyItemChanged(position)


                                    }.show()

                                return@setOnMenuItemClickListener true
                            }
                            R.id.popup_text_styling -> {

                                val fontsData = baseActivity.getFontsData()
                                val fontPicker = FontPickerDialogFragment(fontsData
                                ) { textFont, textFontPosition ->

                                    // modify task item to save in the database
                                    if (currentTask.task.task_font != null){
                                        currentTask.task.task_font?.textFontName = textFont.file_name
                                    } else{
                                        val font = CustomStyle(textFontName = textFont.file_name)
                                        currentTask.task.task_font = font
                                    }


                                    currentTask.subtaskList.forEach {
                                        if (it.subtask_font != null){
                                            it.subtask_font?.textFontName = textFont.file_name
                                        } else{
                                            val font = CustomStyle(textFontName = textFont.file_name)
                                            it.subtask_font = font
                                        }
                                    }

                                    // update in database
                                    clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)

                                    clickListener(adapterPosition, currentTask, Constants.UPDATE_DB_PLUS)

                                    // notify adapter to redraw this task item`
                                    val adapter = (itemView.parent as RecyclerView).adapter
                                    adapter?.notifyItemChanged(position)


                                }
                                fontPicker.show(baseActivity.supportFragmentManager, "font_picker")

                                return@setOnMenuItemClickListener true
                            }
                            R.id.popup_clear_format -> {
                                // clear the formatting and apply default format
                                val font = CustomStyle(null, null,
                                    null, null)

                                currentTask.task.task_font = font

                                currentTask.subtaskList.forEach {
                                    it.subtask_font = font
                                }

                                // update in database
                                clickListener(adapterPosition, currentTask, Constants.UPDATE_DB)

                                clickListener(adapterPosition, currentTask, Constants.UPDATE_DB_PLUS)

                                // notify adapter to redraw this task item`
                                val adapter = (itemView.parent as RecyclerView).adapter
                                adapter?.notifyItemChanged(position)

                                return@setOnMenuItemClickListener true
                            }
                            R.id.popup_delete_task_item -> {
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

        companion object {
            fun create(parent: ViewGroup, activity: BaseActivity): TaskRecycleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_item, parent, false)
                return TaskRecycleViewHolder(view, activity)
            }
        }

        private fun onListSubtaskLayoutClick(position: Int, subtask: Subtask) {
            //clickListener(adapterPosition, currentTask, Constants.OPEN_EDITOR)
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

        // Override onTouch() method to implement touch listener
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            // Handle touch events here
            // Return true to indicate that touch event is handled
            Log.v(Constants.TAG, "onTouch")
            if (event != null) {
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    Log.v(Constants.TAG, "ViewHolder has touch listener set")
                }
            }
            return false
        }

        // Implement other required methods of ItemTouchHelperViewHolder
        override fun onItemSelected() {
            // Provide feedback to user when item is being dragged
            itemView.setBackgroundColor(Color.LTGRAY)
            Log.v(Constants.TAG, "onItemSelected")
        }

        override fun onItemClear() {
            // Provide feedback to user when item is released after being dragged
            itemView.setBackgroundColor(Color.WHITE)
            Log.v(Constants.TAG, "onItemClear")
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