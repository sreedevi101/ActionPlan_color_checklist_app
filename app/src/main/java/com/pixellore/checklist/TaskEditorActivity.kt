package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.Task
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants
import com.pixellore.checklist.utils.MultipurposeAlertDialogFragment
import java.util.HashMap

/**
 * Activity to create new task item or edit the existing task item
 * */
class TaskEditorActivity : BaseActivity() {

    private lateinit var editTaskTitleView: EditText
    private lateinit var editTaskDetailsView: EditText
    private lateinit var addDueDateView: EditText

    private lateinit var subtaskLayout: LinearLayout
    private lateinit var addSubtaskButton: Button

    private lateinit var editorLayout: RelativeLayout
    private lateinit var secondaryColorStrip: LinearLayout

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>


    // Intent to pass the output  back to caller activity
    private val replyIntent = Intent()

    // Input data received if the activity is opened in edit mode (by clicking an existing task)
    private var taskReceived: Task? = null
    private val subtaskListReceived = mutableListOf<Subtask?>()
    private var subtaskListSizeToEdit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_task_editor)

        editTaskTitleView = findViewById(R.id.edit_task_title)
        editTaskDetailsView = findViewById(R.id.edit_task_details)
        addDueDateView = findViewById(R.id.add_due_date)

        editorLayout = findViewById(R.id.editor_activity_layout)
        secondaryColorStrip = findViewById(R.id.secondary_color_design)

        val button = findViewById<Button>(R.id.button_save)
        val cancelBtn = findViewById<Button>(R.id.cancel_button)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.task_editor_activity_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add a new task"
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get colors from current theme, so it can applied to the background
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // design with secondary color
        currentThemeColors["colorSecondary"]?.let { secondaryColorStrip.setBackgroundColor(it) }
        currentThemeColors["colorPrimary"]?.let { button.setTextColor(it) }
        currentThemeColors["colorPrimary"]?.let { cancelBtn.setTextColor(it) }

        // set toolbar background color to colorPrimary of the current theme
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { toolbar.setBackgroundColor(it) }
        }
        // set toolbar title text color to colorOnPrimary of the current theme
        if (currentThemeColors.containsKey("colorOnPrimary")) {
            currentThemeColors["colorOnPrimary"]?.let { toolbar.setTitleTextColor(it) }

            // set back arrow color
            val nav = toolbar.navigationIcon
            if (nav!=null){
                currentThemeColors["colorOnPrimary"]?.let { nav.setTint(it) }
            }
        }

        /**
         * Add an Edit text view when the 'addSubtaskButton' button is pressed
         * */
        subtaskLayout = findViewById(R.id.subtask_layout)
        addSubtaskButton = findViewById(R.id.add_subtask_btn)

        addSubtaskButton.setOnClickListener {
            // call function to create, and style the edit text view, delete button and associated layout
            val subtaskLayoutGroup = addSubtaskViewGroup()
            subtaskLayout.addView(subtaskLayoutGroup, subtaskLayout.childCount)
        }

        /**
         * Already existing Task and Subtasks passed to this Intent to edit
         * */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
            taskReceived = intent.getParcelableExtra("Task", Task::class.java)
        } else {
            taskReceived = intent.getParcelableExtra("Task")
        }


        subtaskListSizeToEdit = intent.getIntExtra("NoOfSubtasks", 0)

        var eachSubtask: Subtask?
        for (count in 0..subtaskListSizeToEdit) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                eachSubtask = intent.getParcelableExtra("Subtask_$count", Subtask::class.java)
            } else {
                eachSubtask = intent.getParcelableExtra("Subtask_$count")
            }

            subtaskListReceived.add(eachSubtask)
        }

        /**
         * In edit mode, Set the current Task and Subtask details to text views
         * */
        if (taskReceived != null) {
            editTaskTitleView.setText(taskReceived!!.task_title)
            editTaskDetailsView.setText(taskReceived!!.details_note)
            addDueDateView.setText(taskReceived!!.due_date)
            supportActionBar?.title = "Edit the task" // toolbar title in edit mode
        }
        // If there are subtasks..
        if (subtaskListReceived.isNotEmpty()) {
            // DEBUG
            //printSubtasks(subtaskListSizeToEdit, subtaskListReceived)
            // .. iterate through the subtasks ..
            for (count in 0 until subtaskListSizeToEdit) {
                // get each subtask title
                val subtaskTitleToShow = subtaskListReceived[count]?.subtask_title
                // get subtask id to save as reference
                val subtaskIdToSave = subtaskListReceived[count]?.subtask_id
                // ..add a layout to display this subtask..
                val subtaskGroupCreated = addSubtaskViewGroup()
                subtaskLayout.addView(subtaskGroupCreated, subtaskLayout.childCount)

                // ..get the edit text view in the layout (only one EditText view in this layout)..
                val editTextView = getChildEditText(subtaskGroupCreated)
                if (editTextView != null){
                    // ..show the subtask title in the edit text view
                    editTextView.setText(subtaskTitleToShow)

                    // add subtask id as tag in edit text view for reference
                    editTextView.setTag(R.id.subtask_id_as_tag, subtaskIdToSave)
                }

            }
        }


        /**
         * Save the changes - when the save button is pressed, get the texts from all the Edit Text views,
         * and pass them back to the caller activity (ChecklistActivity)
         * */

        button.setOnClickListener {

            // Get the Task title
            val title = editTaskTitleView.text.toString()
            //Log.v(Constants.TAG, title)

            // if task title is empty - warn the user
            if (TextUtils.isEmpty(title)) {

                // When title is empty and user press SAVE
                // Warn that title is empty and task cannot be saved
                val myDialog = MultipurposeAlertDialogFragment.newInstance(
                    headline = "Task Title missing",
                    text = "Cannot save this task with no title. Enter a task title",
                    posButtonText = "Got it!",
                    negButtonText = ""
                ) {
                    // empty body
                }
                myDialog.setBaseActivityListener(this)
                myDialog.show(supportFragmentManager, "dialog")

            }
            else {
                // if task title is not empty, collect texts from all other edit text views

                val taskUpdated = extractData()
                replyIntent.putExtra(TASK, taskUpdated)

                // Subtasks
                val subtasksUpdatedList = extractDataSubtasks()

                if (subtasksUpdatedList.isNotEmpty()){
                    val subtasksUpdatedArray = subtasksUpdatedList.toTypedArray()
                    replyIntent.putParcelableArrayListExtra(SUBTASK_LIST,
                        ArrayList(subtasksUpdatedArray.toList()))
                }

                if (subtaskListReceived.isNotEmpty()){
                    val changeLabelList = identifySubtaskModifications(subtaskListReceived, subtasksUpdatedList)
                    replyIntent.putExtra(SUBTASK_LABEL, ArrayList(changeLabelList))
                }

                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }

        cancelBtn.setOnClickListener {
            goHomeAction()

        }
    }

    // Default values for Task and Subtask objects
    companion object {
        // Defaults for Task
        const val TASK_DETAILS = ""
        const val TASK_DUE_DATE = ""
        const val TASK_PRIORITY = "None"
        const val TASK_IS_EXPANDED = false
        const val TASK_IS_COMPLETED = false
        // Unknowns for Task - fill a temporary value
        const val TASK_ID = -2
        const val TASK_PARENT_CHECKLIST_ID = -2
        const val TASK_POSITION_ID = -2
        // For Subtask
        const val SUBTASK_IS_COMPLETED = false
        // Unknowns for Subtask - fill a temporary value
        const val SUBTASK_ID = -2
        const val SUBTASK_PARENT_TASK_ID = -2
        const val SUBTASK_POSITION_ID = -2

        // Constants as intent keys
        const val TASK = "com.pixellore.checklist.TASK"
        const val SUBTASK_LIST = "com.pixellore.checklist.SUBTASK_LIST"
        const val SUBTASK_LABEL = "com.pixellore.checklist.SUBTASK_LABEL"
    }


    private fun goHomeAction(){

        val taskUpdated = extractData()
        val subtasksUpdatedList = extractDataSubtasks()

        // if edited
        val edited = isEdited(taskUpdated,
            subtasksUpdatedList = subtasksUpdatedList)


        //Log.v(Constants.TAG, " taskUpdated ${taskUpdated.task_title?.isEmpty()}")
        Log.v(Constants.TAG, " subtasksUpdatedList $subtasksUpdatedList")
        Log.v(Constants.TAG, " subtasksUpdatedList ${subtasksUpdatedList.isEmpty()}")

        if (edited){
            // confirm action
            val myDialog = MultipurposeAlertDialogFragment.newInstance(
                headline = "Discard changes?",
                text = "The changes made will be lost if exit before saving. ",
                posButtonText = "Discard",
                negButtonText = "Cancel"
            ) {
                onBackPressed()
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }

            myDialog.setBaseActivityListener(this)
            myDialog.show(supportFragmentManager, "dialog")
        } else { // no changes made
            onBackPressed()
            setResult(Activity.RESULT_CANCELED, replyIntent)
        }
    }

    /*
    * EDIT mode
    * --------
    * compare the task and subtasks received with the task and subtasks created from this (TaskEditorActivity)
    *
    * 'equals()' function is override in the data classes for this purpose
    *
    * NEW task mode
    * -------------
    *
    * checks if any of the fields title, due date, details or subtask list are non-empty
    *
    * Result from the comparison is used to determine if a warning dialog should be shown or not while
    * pressing back arrow or 'cancel' button
    * */
    private fun isEdited(
        taskUpdated: Task,
        subtasksUpdatedList: MutableList<Subtask>) : Boolean{

        var editedFlag = false
        if (taskReceived != null){ // EDIT mode
            if (taskUpdated != taskReceived){
                // editing happened
                editedFlag = true
            }
            if (subtasksUpdatedList.size == subtaskListReceived.size){
                subtasksUpdatedList.zip(subtaskListReceived).forEach { (updated, received) ->
                    if (updated != received){
                        // editing happened
                        editedFlag = true
                    }
                }
            } else {
                // editing happened
                editedFlag = true
            }
        } else { // NEW task mode
            if (taskUpdated.task_title?.isNotEmpty() == true ||
                taskUpdated.due_date?.isNotEmpty() == true ||
                taskUpdated.details_note?.isNotEmpty()  == true ||
                subtasksUpdatedList.isNotEmpty()){
                // editing happened
                editedFlag = true
            }
        }
        return editedFlag
    }
    private fun extractData() : Task{
        /**
         * In Edit mode, get the ids from the task (received from caller activity) and
         * pass it back to the caller activity
         */
        val title = editTaskTitleView.text.toString()
        val taskId: Int
        val taskPosId: Int
        val details: String?
        val dueDate:String?
        val priority: String?
        val isExpanded:Boolean
        val isCompleted: Boolean
        val taskParentChecklistId: Int
        val taskFont: CustomStyle
        if (taskReceived != null){ // EDIT mode
            // If task is received,
            taskId = taskReceived!!.task_id
            taskPosId = taskReceived!!.task_pos_id
            taskParentChecklistId = taskReceived!!.parent_checklist_id
            taskFont = taskReceived!!.task_font!!

            // task details
            if (editTaskDetailsView.text.toString().isNotEmpty()) {
                details = editTaskDetailsView.text.toString()
            } else {
                details = taskReceived!!.details_note
            }

            // task due date
            if (addDueDateView.text.toString().isNotEmpty()) {
                dueDate = addDueDateView.text.toString()
            } else {
                dueDate = taskReceived!!.due_date
            }

            priority = taskReceived!!.priority
            isExpanded = taskReceived!!.isExpanded
            isCompleted = taskReceived!!.task_isCompleted
        } else{  // NEW task mode
            // if no task received, use dummy values temporarily to create the object
            // these fields will be updated with correct values in ChecklistActivity before
            // inserting in the database
            taskId = TASK_ID
            taskPosId = TASK_POSITION_ID
            taskParentChecklistId = TASK_PARENT_CHECKLIST_ID
            taskFont = CustomStyle(null, null,
                null, null)

            // task details
            if (editTaskDetailsView.text.toString().isNotEmpty()) {
                details = editTaskDetailsView.text.toString()
            } else {
                details = TASK_DETAILS
            }

            // task due date
            if (addDueDateView.text.toString().isNotEmpty()) {
                dueDate = addDueDateView.text.toString()
            } else {
                dueDate = TASK_DUE_DATE
            }

            // fill with defaults
            priority = TASK_PRIORITY
            isExpanded = TASK_IS_EXPANDED
            isCompleted = TASK_IS_COMPLETED
        }

        /**
         * Create a task object to pass back to the caller activity
         *
         * This task will be updated in the database by the caller activity ChecklistActivity
         *
         * Default values are assigned to optional fields if user not filled data and
         * temporary values are assigned to fields like ids (will be updated in the caller activity)
         */
        val taskUpdated = Task(taskId, taskPosId, title, details, dueDate,
            priority, isExpanded, isCompleted, taskParentChecklistId, taskFont)

        return taskUpdated

    }

    private fun extractDataSubtasks(): MutableList<Subtask> {

        val subtaskListFromEditor = mutableListOf<MutableMap<String, Any?>>()
        // Get subtasks titles from programmatically created edit text views groups
        // we are looking for EditTextView in
        // 'subtaskLayout' -> ViewGroup -> EditTextView
        for (i in 0 until subtaskLayout.childCount){
            // iterate through children of 'subtaskLayout'
            // if child is a ViewGroup ..
            if (subtaskLayout.getChildAt(i) is ViewGroup){
                val childLayout = subtaskLayout.getChildAt(i) as ViewGroup
                // ..iterate through children of that view group..
                for (j in 0 until childLayout.childCount){
                    val child = childLayout.getChildAt(j)
                    // .. if child is edit text,..
                    if (child is EditText){
                        // fill subtask id (from edit text tag) and subtask title (from edit text)
                        // in this dictionary
                        val dictSubtaskRef = mutableMapOf<String, Any?>()
                        // ..get subtask title from edit text view
                        val subtaskTitle = child.text.toString()
                        // add subtask titles to an array to pass to caller activity
                        if (subtaskTitle.isNotEmpty()) {
                            // title cannot be empty
                            val subtaskIdFromTag = child.getTag(R.id.subtask_id_as_tag) as? Int
                            dictSubtaskRef["id"] = subtaskIdFromTag
                            dictSubtaskRef["title"] = subtaskTitle
                            subtaskListFromEditor.add(dictSubtaskRef)
                        }
                    }
                }
            }
        }

        // array of subtask objects to send back to caller activity
        val subtasksUpdatedList = mutableListOf<Subtask>()
        //  add subtask object array to pass to caller activity
        if (subtaskListFromEditor.isNotEmpty()) {
            // create subtask objects
            subtaskListFromEditor.forEach { dict ->
                val subtaskId:Int
                if (dict["id"] != null){
                    subtaskId = dict["id"] as Int
                } else{
                    subtaskId = SUBTASK_ID
                }

                val subtaskTitle:String = dict["title"] as String

                // DEBUG print all subtasks
                //Log.v(Constants.TAG, "Subtask from edit texts: $subtaskId - $subtaskTitle")

                var subtaskParentTaskId = SUBTASK_PARENT_TASK_ID
                var subtaskPosId = SUBTASK_POSITION_ID
                var subtaskIsCompleted = SUBTASK_IS_COMPLETED
                var font = CustomStyle(null, null,
                    null, null)
                // If there are subtasks..
                if (subtaskListReceived.isNotEmpty()) {
                    // .. iterate through the subtasks ..
                    for (count in 0 until subtaskListSizeToEdit) {
                        if (subtaskListReceived[count] != null){
                            // if subtask is not null
                            if (subtaskListReceived[count]?.subtask_id == subtaskId) {
                                // if subtask id matches the id from the edit text view tag

                                // get other filed like parent_task_id, subtask_isCompleted
                                subtaskParentTaskId = subtaskListReceived[count]?.parent_task_id!!
                                subtaskPosId = subtaskListReceived[count]?.subtask_pos_id!!
                                subtaskIsCompleted = subtaskListReceived[count]?.subtask_isCompleted!!
                                font = subtaskListReceived[count]?.subtask_font!!
                            }
                        }
                    }
                }


                // Create subtask object
                val subtaskUpdated = Subtask(subtaskId, subtaskPosId, subtaskParentTaskId,
                    subtaskTitle, subtaskIsCompleted, font)

                subtasksUpdatedList.add(subtaskUpdated)
            }
        }
        return subtasksUpdatedList
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // action same as cancel button
                goHomeAction()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Compare the subtask MutableLists
     * (1) received via intent from database when this activity was opened, and
     * (2) created from the activity layout by reading the Edit text view texts
     *
     * the purpose is to compare and identify which are the ones that got deleted/updated/newly inserted
     *
     * the result saved in a dictionary along with the id.
     *
     * newly inserted ones don't have any id (id = null), they are thus not included in the dict
     * */
    private fun identifySubtaskModifications(listFromDb:MutableList<Subtask?>,
                                             listWithChanges:MutableList<Subtask>):
            MutableList<MutableMap<String, Any>>{

        val labels = mutableListOf<MutableMap<String, Any>>()

        // find subtasks IDs 'listFromDb' not present in 'listWithChanges' - DELETED
        listFromDb.forEach { subtaskFromDb ->

            if (subtaskFromDb!=null){
                var idFoundFlag = false

                // create an empty mutable dictionary
                val dict = mutableMapOf<String, Any>()

                // loop through 'listWithChanges' searching for subtask with Id same as that of '2.4
                // subtaskFromDb'
                listWithChanges.forEach { subtaskWithChanges ->
                    if (subtaskFromDb.subtask_id == subtaskWithChanges.subtask_id){
                        dict["id"] = subtaskFromDb.subtask_id
                        if (subtaskFromDb.subtask_title == subtaskWithChanges.subtask_title){
                            dict["label"] = "NO_CHANGE"
                        } else {
                            dict["label"] = "UPDATE"
                        }
                        labels.add(dict)
                        idFoundFlag = true
                        // exit 'listWithChanges' loop after finding same id in 'listWithChanges'
                    }
                }

                if (!idFoundFlag){
                    dict["id"] = subtaskFromDb.subtask_id
                    dict["label"] = "DELETE"
                    labels.add(dict)
                }

            }
        }
        return labels
    }

    /**
     * Create and style an Edit Text View and return it
     *
     * This is for subtask title
     * */
    private fun addEditTextView(): EditText{
        val subtaskEditText = EditText(this)
        subtaskEditText.hint = getString(R.string.add_subtask)
        subtaskEditText.setTextAppearance(R.style.edittext_task_editor)
        // transparent background
        subtaskEditText.setBackgroundColor(
            resources.getColor(
                android.R.color.transparent,
                theme
            )
        )
        // text color and hint text color
        currentThemeColors["colorOnSecondary"]?.let { subtaskEditText.setTextColor(it) }
        currentThemeColors["colorOnSecondary"]?.let { subtaskEditText.setHintTextColor(it) }
        // set layout params
        subtaskEditText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        return subtaskEditText
    }


    /**
     * Create and style an Image Button View and return it
     *
     * This is for subtask delete button
     * */
    private fun addImageButtonView(): ImageButton{
        val closeButton = ImageButton(this)
        closeButton.contentDescription = getString(R.string.delete_subtask)
        closeButton.setImageResource(R.drawable.baseline_close_24)
        closeButton.maxHeight = 48
        closeButton.minimumWidth = 48

        closeButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        // Delete subtask layout group on button click
        closeButton.setOnClickListener {
            // get the parent layout (linear layout with this button and subtask title edit text view) using tag
            val subtaskInsideGroup = closeButton.tag as ViewGroup
            // get the parent to this layout (parent is R.id.subtask_layout)
            val subtaskLayoutOverall = subtaskInsideGroup.parent as ViewGroup

            // get tag from edit text to pin point which subtask got deleted
            // edit text tag has subtask id
            val childEditText = getChildEditText(subtaskInsideGroup)
            if (childEditText != null) {
                val subtaskIdToBeDeleted = childEditText.getTag(R.id.subtask_id_as_tag)

                // DEBUG
                Log.v(Constants.TAG, "Subtask being deleted is: " + childEditText.text +
                        " (with id: " + subtaskIdToBeDeleted + " )")
            }

            val myDialog = MultipurposeAlertDialogFragment.newInstance(
                headline = "Remove subtask?",
                text = "",
                posButtonText = "Remove",
                negButtonText = "Cancel"
            ) {
                // remove the layout containing this button from its parent
                subtaskLayoutOverall.removeView(subtaskInsideGroup)
            }

            myDialog.setBaseActivityListener(this)

            myDialog.show(supportFragmentManager, "dialog")

        }

        return closeButton
    }


    /**
     * Create and style a LinearLayout and return it
     *
     * This is for subtask edit text view and delete image button
     * */
    private fun addSubtaskViewGroup(): LinearLayout{
        val subtaskLayoutGroup = LinearLayout(this)
        subtaskLayoutGroup.orientation = LinearLayout.HORIZONTAL

        subtaskLayoutGroup.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


        // add edit text view for subtask title and image button to delete subtask
        val textView = addEditTextView()
        val imageButton = addImageButtonView()

        // set tag to retrieve parent layout - used in button setOnClickListener callback
        imageButton.tag = subtaskLayoutGroup

        // add views
        subtaskLayoutGroup.addView(textView, subtaskLayoutGroup.childCount)
        subtaskLayoutGroup.addView(imageButton, subtaskLayoutGroup.childCount)

        return subtaskLayoutGroup
    }

    /*
    * Return the Edit text child view in a view group
    *
    * assuming there is only one Edit text child view,
    * if  more than one, first one will be returned.
    * Returns null if no edit text view found
    * */
    private fun getChildEditText(viewGroup: ViewGroup): EditText? {
        for (j in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(j)
            // .. if child is edit text,..
            if (child is EditText) {
                return child
            }
        }
        return null
    }


    override fun onResume() {
        super.onResume()
        setTheme()
        if (TaskApplication.recreateTaskEditor){
            recreate()
            TaskApplication.recreateTaskEditor = false
        }
    }

    override fun onStart() {
        super.onStart()
        setTheme()
        if (TaskApplication.recreateTaskEditor){
            recreate()
            TaskApplication.recreateTaskEditor = false
        }
    }
}