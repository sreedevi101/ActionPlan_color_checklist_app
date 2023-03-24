package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pixellore.checklist.AdapterUtility.TaskRecycleAdapter
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants
import com.pixellore.checklist.utils.MultipurposeAlertDialogFragment
import com.pixellore.checklist.utils.StyleSelectionDialogFragment
import java.util.HashMap

class ChecklistActivity : BaseActivity() {

    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    /*
    * List of Task and Subtask Ids obtained as LiveData
    * This is used to find out the unque ID to be assigned to the next task item and subtask item
    *
    * Task unique id irrespective of the checklist it belongs to, its unique among all the tasks objects.
    * same about subtasks as well
    *
    * unique id is assigned in the order the object is created
    * */
    private var taskIds: List<Int> = emptyList()
    private var subtaskIds: List<Int> = emptyList()


    // checklist object; clicking on this object opens this activity
    private var checklistToDisplay: Checklist? = null

    // RecyclerView
    private lateinit var actionListRecyclerView: RecyclerView
    private val adapter = TaskRecycleAdapter(
        { position, taskWithSubtasks, actionRequested ->
            onListItemClick(
                position,
                taskWithSubtasks,
                actionRequested
            )
        },
        { position, subtask -> onListSubtaskClick(position, subtask) },
        this)

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>

    // save a copy of the data being displayed (to Share as text and to apply same style to all tasks)
    private lateinit var taskWithSubtasksList: List<TaskWithSubtasks>


    // Receiver for data from TaskEditorActivity - if TaskEditorActivity is opened to create new task
    private val getItemAddActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {

                // Receive task object and array of subtask objects from editor activity

                /**
                 * Updated Task object from TaskEditorActivity
                 * */
                val taskUpdatedReceived: Task?
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    taskUpdatedReceived =
                        it.data?.getParcelableExtra(TaskEditorActivity.TASK, Task::class.java)
                } else {
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK)
                }

                /**
                 * Updated Subtask objects array from TaskEditorActivity
                 * */
                val subtasksUpdatedReceivedArray: Array<Subtask?>
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST, Subtask::class.java
                    ) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                } else {

                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST
                    ) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                }


                // continue to add the Task if only it has a  parent Checklist
                if (checklistToDisplay != null) {

                    // find id to be used for the new task
                    val taskId = findNextId(taskIds)

                    if (taskId == -1){ // unique ids reached max limit of Int (Int.MAX_VALUE = 2147483647)
                        // alert dialog saying reached max limit
                        val myDialog = MultipurposeAlertDialogFragment.newInstance(
                            headline = "Maximum Limit Reached",
                            text = "The maximum limit of ${Int.MAX_VALUE} task unique IDs " +
                                    "has been reached. You cannot create any more tasks.",
                            posButtonText = "OK",
                            negButtonText = ""
                        ) {

                        }
                        myDialog.setBaseActivityListener(this)
                        myDialog.show(supportFragmentManager, "dialog")

                    } else {
                        // get parent checklist id from the Checklist object passed to this intent when opening it
                        val parentChecklistId = checklistToDisplay!!.checklist_id


                        // get position of the new task
                        val taskPosId = findNextPositionId(taskIds)

                        // IMPORTANT STEP
                        // Add correct values for IDs
                        if (taskUpdatedReceived != null) {
                            taskUpdatedReceived.task_id = taskId
                            taskUpdatedReceived.task_pos_id = taskPosId
                            taskUpdatedReceived.parent_checklist_id = parentChecklistId

                            // Insert new task
                            actionPlanViewModel.insertTask(taskUpdatedReceived)

                            // insert corresponding subtasks
                            subtasksUpdatedReceivedArray.forEach { subtask ->
                                if (subtask != null) {
                                    // IMPORTANT STEP
                                    // Add correct values for IDs
                                    val subtaskId = findNextId(subtaskIds)
                                    if (subtaskId == -1){ // unique ids reached max limit of Int
                                        // alert dialog saying reached max limit
                                        val myDialog = MultipurposeAlertDialogFragment.newInstance(
                                            headline = "Maximum Limit Reached",
                                            text = "The maximum limit of ${Int.MAX_VALUE} subtask unique IDs " +
                                                    "has been reached. You cannot create any more subtasks.",
                                            posButtonText = "OK",
                                            negButtonText = ""
                                        ) {

                                        }
                                        myDialog.setBaseActivityListener(this)
                                        myDialog.show(supportFragmentManager, "dialog")

                                    } else{

                                        // get position of this subtask
                                        val subtaskPosId = findNextPositionId(subtaskIds)

                                        subtask.subtask_id = subtaskId
                                        subtask.subtask_pos_id = subtaskPosId
                                        subtask.parent_task_id = taskId

                                        Log.v(Constants.TAG, "Subtask to be Inserted " +
                                                "${subtask.subtask_id} - ${subtask.subtask_title} " +
                                                "- parent task id ${subtask.parent_task_id}")

                                        actionPlanViewModel.insertSubtask(subtask)

                                        val currentSubtaskIds = subtaskIds.orEmpty().toMutableList()
                                        currentSubtaskIds.add(subtaskId) // id added now
                                        subtaskIds = currentSubtaskIds
                                    }
                                }
                            }
                        }

                    }


                } else {
                    Log.v(Constants.TAG, "Parent Checklist missing. Task cannot be added")

                    Toast.makeText(
                        applicationContext,
                        "Parent Checklist missing. Task cannot be added",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            else if(it.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    applicationContext,
                    "Task not saved", Toast.LENGTH_LONG
                ).show()
            }
            else {
                Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG)
                    .show()
            }
        }

    // Receiver for data from TaskEditorActivity - if TaskEditorActivity is opened to edit existing task
    private val getItemEditActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // Receive task object and array of subtask objects from editor activity

                /**
                 * Updated Task object from TaskEditorActivity
                 * */
                val taskUpdatedReceived: Task?
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    taskUpdatedReceived =
                        it.data?.getParcelableExtra(TaskEditorActivity.TASK, Task::class.java)
                } else {
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK)
                }

                /**
                 * Updated Subtask objects array from TaskEditorActivity
                 * */
                val subtasksUpdatedReceivedArray: Array<Subtask?>
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST, Subtask::class.java
                    ) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                } else {

                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST
                    ) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                }


                /**
                 * Subtask labels - list of dictionary with keys "id" and "label"
                 *
                 * ids corresponds to the  subtask ids sent to TaskEditorActivity (already existing
                 * in database as subtasks of this parent task)
                 *
                 * labels mention the action to be done based on the modification made by user
                 * in the TaskEditorActivity layout
                 * labels - DELETE, UPDATE, NO_CHANGE
                 *
                 * Note: Newly created tasks are not in this list. If any subtask id received is not
                 * in this list of dicts, its a new subtask ane need to be inserted into the database
                 * */
                val subtaskLabelList: ArrayList<MutableMap<String, Any>>?
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                    subtaskLabelList = it.data?.getSerializableExtra(
                        TaskEditorActivity.SUBTASK_LABEL,
                        ArrayList::class.java
                    ) as ArrayList<MutableMap<String, Any>>?

                } else {
                    // Use Serializable
                    subtaskLabelList =
                        it.data?.getSerializableExtra(TaskEditorActivity.SUBTASK_LABEL)
                                as ArrayList<MutableMap<String, Any>>?
                }


                /*
                // DEBUG
                if (subtaskLabelList == null) {
                    Log.v(Constants.TAG, "Subtasks list received is NULL")
                } else if (subtaskLabelList.isEmpty()) {
                    Log.v(Constants.TAG, "Subtasks list received is EMPTY")
                }
                subtaskLabelList?.forEach { dictItem ->
                    Log.v(Constants.TAG, "subtaskLabelList ${dictItem["id"]} - ${dictItem["label"]} ") }
                */


                // continue to add the Task if only it has a  parent Checklist
                if (checklistToDisplay != null) {

                    if (taskUpdatedReceived != null) {

                        // Update new task
                        actionPlanViewModel.updateTask(taskUpdatedReceived)


                        // Update/Insert corresponding subtasks - Delete is separate

                        // iterate through all the subtasks received from TaskEditorActivity
                        subtasksUpdatedReceivedArray.forEach { subtask ->
                            if (subtask != null) {

                                /*
                                 If any dictionary in the list has a value of 'subtask.subtask_id' for the "id" key,
                                  the value of isIdMatched will be true. Otherwise, it will be false.
                                * */
                                val isIdMatched = subtaskLabelList?.any { dict ->
                                    dict["id"] == subtask.subtask_id
                                }
                                // if 'isIdMatched' is false that means it is a new subtask and
                                // has to be inserted to database

                                // ********* INSERT *************
                                if (!isIdMatched!!) {

                                    // IMPORTANT STEP
                                    // Add correct values for IDs
                                    val subtaskId = findNextId(subtaskIds)
                                    if (subtaskId == -1){
                                        // alert dialog saying reached max limit
                                        val myDialog = MultipurposeAlertDialogFragment.newInstance(
                                            headline = "Maximum Limit Reached",
                                            text = "The maximum limit of ${Int.MAX_VALUE} subtasks unique IDs " +
                                                    "has been reached. You cannot create any more subtasks.",
                                            posButtonText = "OK",
                                            negButtonText = ""
                                        ) {

                                        }
                                        myDialog.setBaseActivityListener(this)
                                        myDialog.show(supportFragmentManager, "dialog")

                                    } else{
                                        subtask.subtask_id = subtaskId
                                        subtask.parent_task_id = taskUpdatedReceived.task_id

                                        actionPlanViewModel.insertSubtask(subtask)

                                        val currentSubtaskIds = subtaskIds.orEmpty().toMutableList()
                                        currentSubtaskIds.add(subtaskId) // id added now
                                        subtaskIds = currentSubtaskIds
                                    }

                                } else {
                                    // if 'isIdMatched' is true, the id is already available, meaning its an
                                    // update/ delete or no change

                                    // get the "label"
                                    val matchedDict = subtaskLabelList.find { dict ->
                                        dict["id"] == subtask.subtask_id
                                    }
                                    val label = matchedDict?.get("label")

                                    when (label) {
                                        "UPDATE" -> actionPlanViewModel.updateSubtask(subtask)
                                    }

                                }
                            }
                        }

                        // To delete subtasks from database, iterate through list of dict of labels
                        // returned from TaskEditorActivity

                        // get the "ids" with "label" DELETE
                        val deleteIds = subtaskLabelList
                            ?.filter { dict ->
                                dict["label"] == "DELETE"
                            }
                            ?.mapNotNull { dict ->
                                dict["id"] as? Int
                            }

                        Log.v(Constants.TAG, "deleteIds: $deleteIds")
                        if (deleteIds != null) {
                            actionPlanViewModel.deleteSubtasks(deleteIds)
                        }
                        Toast.makeText(applicationContext, "Updated ${taskUpdatedReceived.task_title}",
                            Toast.LENGTH_LONG).show()
                    }

                } else {
                    Log.v(Constants.TAG, "Parent Checklist missing. Task cannot be added")

                    Toast.makeText(applicationContext,
                        "Error: Parent Checklist missing. Task cannot be updated!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if(it.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext,
                    "Task not saved", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext,
                    "Error: Something went wrong. Could not update!", Toast.LENGTH_LONG).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_checklist)


        // get data from calling activity
        if (Build.VERSION.SDK_INT >= 33) {
            checklistToDisplay = intent.getParcelableExtra("Checklist", Checklist::class.java)
        } else {
            checklistToDisplay = intent.getParcelableExtra<Checklist>("Checklist")
        }

        Log.v(
            Constants.TAG,
            "Opening Checklist: " + (checklistToDisplay?.checklist_title ?: "Checklist is null")
        )

        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = checklistToDisplay?.checklist_title ?: "Checklist"
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


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

        actionListRecyclerView = findViewById<RecyclerView>(R.id.actionListRecyclerView)

        // change background color according to checklist item color
        val bgColor = checklistToDisplay?.font?.backgroundColorResId
        if (bgColor != null) {
            actionListRecyclerView.setBackgroundColor(bgColor)
        }

        /*
        * This is to remove the flickering of items in the recycler view when the item is updated
        * */
        actionListRecyclerView.itemAnimator?.changeDuration = 0




        actionListRecyclerView.adapter = adapter
        actionListRecyclerView.layoutManager = LinearLayoutManager(this)

        // get only TasksWithSubtasks belongs to this checklist
        // to display only TasksWithSubtasks belonging to this checklist, when the user opens the checklist
        if (checklistToDisplay != null) {
            actionPlanViewModel.allTasksWithSubtasksByChecklistId(checklistToDisplay!!.checklist_id)
                .observe(this) { tasks ->
                    taskWithSubtasksList = tasks
                    // Update the cached copy of the tasks in the adapter.
                    tasks.let {
                        adapter.submitList(it)
                    }
                }
        }
        else{
            // Unlikely scenario: the checklist activity opened without a checklist (checklist object = null)

            Toast.makeText(
                applicationContext,
                "Error: Something gone really bad!!",
                Toast.LENGTH_LONG
            )
                .show()

            // Navigate back to parent activity
            onBackPressed()
        }

        // Observe the list of IDs
        actionPlanViewModel.getAllTaskIds().observe(this) { ids ->
            // Update the list of IDs
            taskIds = ids
            // DEBUG
            //Log.d(Constants.TAG, "All Task IDs: $taskIds")
        }

        // Observe the list of IDs
        actionPlanViewModel.getAllSubtaskIds().observe(this) { ids ->
            // Update the list of IDs
            subtaskIds = ids
            // DEBUG
            //Log.d(Constants.TAG, "All Subtask IDs: $subtaskIds")
        }

        // Floating Button
        val fab = findViewById<FloatingActionButton>(R.id.addItemFab)
        // customize colors
        if (currentThemeColors.containsKey("colorSecondary")) {
            currentThemeColors["colorSecondary"]?.let {

                // Set the background color of the button
                fab.backgroundTintList = ColorStateList.valueOf(it)
            }
        }
        if (currentThemeColors.containsKey("colorOnSecondary")) {
            currentThemeColors["colorOnSecondary"]?.let {
                // Set the icon of the button with a new color
                val drawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_add_24)?.mutate()
                drawable?.setTint(it)
                fab.setImageDrawable(drawable)
            }
        }
        // click listener
        fab.setOnClickListener {
            val intent = Intent(this@ChecklistActivity, TaskEditorActivity::class.java)
            getItemAddActivityResult.launch(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        setTheme()
        if (TaskApplication.recreateChecklistActivity) {
            recreate()
            TaskApplication.recreateChecklistActivity = false
        }
    }

    override fun onStart() {
        super.onStart()
        setTheme()
        if (TaskApplication.recreateChecklistActivity) {
            recreate()
            TaskApplication.recreateChecklistActivity = false
        }
    }


    /**
     * This functions contains the logic that will be implemented when an item
     * (or any view in the item) is clicked
     *
     * This function is passed as an argument to the recycler view adapter
     * */
    private fun onListItemClick(
        position: Int,
        taskWithSubtasks: TaskWithSubtasks,
        actionRequested: Int
    ) {
        //Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT).show()

        if (actionRequested == Constants.UPDATE_DB) {
            actionPlanViewModel.updateTask(taskWithSubtasks.task)
        }else if (actionRequested == Constants.UPDATE_DB_PLUS){
            taskWithSubtasks.subtaskList.forEach {
                actionPlanViewModel.updateSubtask(it)
            }
        } else if (actionRequested == Constants.OPEN_EDITOR) {
            val intent = Intent(this@ChecklistActivity, TaskEditorActivity::class.java)

            // Pass Task and Subtasks to intent to edit
            intent.putExtra("Task", taskWithSubtasks.task)

            var count = 0
            for (subtask in taskWithSubtasks.subtaskList) {
                intent.putExtra("Subtask_$count", subtask)
                count++
            }

            intent.putExtra("NoOfSubtasks", taskWithSubtasks.subtaskList.size)

            getItemEditActivityResult.launch(intent)
        } else if (actionRequested == Constants.DELETE) {
            // confirm delete action
            val myDialog = MultipurposeAlertDialogFragment.newInstance(
                headline = "Delete task?",
                text = "",
                posButtonText = "Delete",
                negButtonText = "Cancel"
            ) {

                // delete this task
                actionPlanViewModel.deleteTask(taskWithSubtasks.task)

                // get the subtasks - already available in taskWithSubtasks.subtaskList

                for (subtask in taskWithSubtasks.subtaskList) {
                    actionPlanViewModel.deleteSubtask(subtask)
                }

            }

            myDialog.setBaseActivityListener(this)

            myDialog.show(supportFragmentManager, "dialog")
        }
    }

    private fun onListSubtaskClick(position: Int, subtask: Subtask) {
        actionPlanViewModel.updateSubtask(subtask)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.checklist_menu, menu)
        if (menu != null) {
            MenuCompat.setGroupDividerEnabled(menu, true)
        };
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_share -> {
            val checklistText = createChecklistText(taskWithSubtasksList)
            Log.v(Constants.TAG,checklistText)

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, checklistText)
                putExtra(Intent.EXTRA_SUBJECT, Constants.APP_NAME +
                        ":" + (checklistToDisplay?.checklist_title ?: "Checklist"))
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)

            true
        }
        R.id.action_rearrange_list -> {
            // todo implement "Rearrange items"
            true
        }
        R.id.action_delete_all -> {
            // User chose "Delete all", delete all the tasks and subtasks in the checklist
            actionPlanViewModel.deleteAllTasks()
            actionPlanViewModel.deleteAllSubtasks()
            true
        }
        R.id.action_change_background -> {
            if (checklistToDisplay != null) {
                // open color picker
                ColorPickerDialog
                    .Builder(this)                        // Pass Activity Instance
                    .setTitle("Choose Color")            // Default "Choose Color"
                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                    .setColorListener { color, colorHex ->
                        // Handle Color Selection
                        //Log.v(Constants.TAG, "Color Selected: $color")
                        //Log.v(Constants.TAG, "Color Selected - Hex: $colorHex")

                        // change background color
                        actionListRecyclerView.setBackgroundColor(color)

                        // modify Checklist item to save in the database
                        if (checklistToDisplay!!.font != null) {
                            checklistToDisplay!!.font?.backgroundColorResId = color
                        } else {
                            val font = CustomStyle(backgroundColorResId = color)
                            checklistToDisplay!!.font = font
                        }

                        // update in database
                        actionPlanViewModel.updateChecklist(checklistToDisplay!!)
                    }.show()
            } else {

                Toast.makeText(applicationContext,
                    "Error:Parent Checklist missing. This feature will not work!",
                    Toast.LENGTH_LONG
                ).show()
            }
            true
        }
        R.id.action_format_all_items -> {
            // "Format all items"
            val selectStyle = StyleSelectionDialogFragment(
                this) {colorOrFont, styleSelection ->  applySelectedStyle(colorOrFont, styleSelection)}
            selectStyle.setBaseActivityListener(this)
            selectStyle.show(supportFragmentManager, "style_selector")
            true
        }
        R.id.action_print_database -> {

            printDbTables()
            true
        }
        android.R.id.home -> {
            // Navigate back to parent activity
            onBackPressed()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }

    private fun applySelectedStyle(colorOrFont: Any, styleSelection: Int) {
        var color: Int? = null
        var font: TextFont? = null

        when (styleSelection) {
            R.id.style_selector_background,
            R.id.style_selector_title_color,
            R.id.style_selector_details_color -> {
                // styleSelection is equal to one of the specified values
                if (colorOrFont is Int){
                    color = colorOrFont
                } else {
                    Log.e(Constants.TAG,"Wrong input type: color (Int) was expected as input 'colorOrFont")
                }
            }
            R.id.style_selector_font -> {
                if (colorOrFont is TextFont){
                    font = colorOrFont
                } else {
                    Log.e(Constants.TAG,"Wrong input type: font (TextFont) was expected as input 'colorOrFont")
                }
            }
            else -> {
                // styleSelection is not equal to any of the specified values
                Log.e(Constants.TAG,"Wrong input for 'styleSelection' to applySelectedStyle function")
            }
        }


        when (styleSelection) {
            R.id.style_selector_background -> {
                taskWithSubtasksList.forEach { taskWithSubtask ->
                    taskWithSubtask.task.task_font?.backgroundColorResId = color
                    actionPlanViewModel.updateTask(taskWithSubtask.task)
                }
            }
            R.id.style_selector_title_color -> {
                taskWithSubtasksList.forEach { taskWithSubtask ->
                    taskWithSubtask.task.task_font?.headingTextColorResId = color
                    actionPlanViewModel.updateTask(taskWithSubtask.task)
                }
            }
            R.id.style_selector_details_color -> {
                taskWithSubtasksList.forEach { taskWithSubtask ->
                    taskWithSubtask.task.task_font?.bodyTextColorResId = color
                    actionPlanViewModel.updateTask(taskWithSubtask.task)

                    taskWithSubtask.subtaskList.forEach {subtask ->
                        subtask.subtask_font?.bodyTextColorResId = color
                        actionPlanViewModel.updateSubtask(subtask)
                    }
                }
            }
            R.id.style_selector_font -> {
                taskWithSubtasksList.forEach { taskWithSubtask ->
                    if (font != null) {
                        taskWithSubtask.task.task_font?.textFontName = font.file_name
                    }
                    actionPlanViewModel.updateTask(taskWithSubtask.task)

                    taskWithSubtask.subtaskList.forEach {subtask ->
                        Log.v(Constants.TAG, "subtask: ${subtask.subtask_title}")
                        if (font != null) {
                            Log.v(Constants.TAG, "subtask: ${subtask.subtask_title}, font ${font.file_name}")
                            subtask.subtask_font?.textFontName = font.file_name
                        }
                        actionPlanViewModel.updateSubtask(subtask)
                    }
                }
            }
            else -> {
                // handle other types of arguments

                Log.e(Constants.TAG,"Wrong input for 'styleSelection' to applySelectedStyle function")
            }
        }

        adapter.notifyDataSetChanged()
    }
    private fun createChecklistText(checklistContent: List<TaskWithSubtasks>): String{

        val fourSpaces = "    "
        var stringOutput = ""

        stringOutput += checklistToDisplay?.checklist_title
        stringOutput += "\n"
        stringOutput += "-".repeat(30)
        stringOutput += "\n"

        if (checklistContent.isEmpty()){
            stringOutput += "There are no tasks in this checklist"
        }

        var i = 1
        checklistContent.let { tasksList ->
            tasksList.forEach {
                stringOutput += "$i. " + it.task.task_title
                if (!it.task.priority.equals("None")){
                    stringOutput += " (${it.task.priority?.uppercase()} priority)"
                }
                stringOutput += "\n"

                if (!it.task.due_date.equals("")){
                    stringOutput += fourSpaces + "Due date: " + it.task.due_date
                    if (it.task.task_isCompleted){
                        stringOutput += " (COMPLETED)"
                    }
                    stringOutput += "\n"
                } else if (it.task.task_isCompleted){
                    stringOutput += fourSpaces
                    stringOutput += "(COMPLETED)"
                    stringOutput += "\n"
                }

                if (!it.task.details_note.equals("")){
                    stringOutput += fourSpaces + it.task.details_note
                    stringOutput += "\n"
                }

                var j = 1
                it.subtaskList.forEach {subtasksList ->
                    stringOutput += fourSpaces.repeat(2) + "$i.$j " + subtasksList.subtask_title
                    stringOutput += "\n"
                    j += 1
                }
                i += 1
                stringOutput += "\n"
            }

        }
        stringOutput += "-".repeat(30)
        stringOutput += "\nSend from " + Constants.APP_NAME
        // todo add "website name" of the app
        return stringOutput
    }

    private fun printDbTables() {
        val tag = Constants.TAG
        var count: Int

        actionPlanViewModel.allChecklistTasks.observe(this) { tasks ->
            val numberOfTaskItems = tasks.size
            Log.v(tag, "There are $numberOfTaskItems tasks")
            tasks.let {
                count = 1
                it.forEach {
                    if (count == 1) {
                        Log.v(
                            tag,
                            "\n\n-----------------------------------------------------------------------"
                        )
                    }
                    Log.v(tag, "${it.task_id} - ${it.task_title}")
                    Log.v(tag, "Due Date: ${it.due_date}")
                    Log.v(tag, "Details: ${it.details_note}")
                    Log.v(tag, "Priority: ${it.priority}")
                    Log.v(tag, "Expanded: ${it.isExpanded}, Completed: ${it.task_isCompleted}")
                    Log.v(tag, "CustomStyle: ${it.task_font}")
                    Log.v(
                        tag,
                        "-----------------------------------------------------------------------"
                    )
                    count++
                }
            }
        }

        actionPlanViewModel.allChecklistSubtasks.observe(this) { subtasks ->
            val numberOfSubtaskItems = subtasks.size
            Log.v(tag, "There are $numberOfSubtaskItems subtasks")
            subtasks.let {
                count = 1
                it.forEach {
                    if (count == 1) {
                        Log.v(
                            tag,
                            "\n\n-----------------------------------------------------------------------"
                        )
                    }
                    Log.v(tag, "${it.subtask_id} - ${it.subtask_title}")
                    Log.v(tag, "Parent ID: ${it.parent_task_id}")
                    Log.v(tag, "Completed: ${it.subtask_isCompleted}")
                    Log.v(tag, "CustomStyle: ${it.subtask_font}")
                    Log.v(
                        tag,
                        "-----------------------------------------------------------------------"
                    )
                    count++
                }
            }
        }


        actionPlanViewModel.allTasksWithSubtasks.observe(this) { tasks ->
            tasks.let {
                count = 1
                it.forEach {
                    if (count == 1) {
                        Log.v(
                            tag,
                            "\n\n-----------------------------------------------------------------------"
                        )
                    }
                    Log.v(tag, "${it.task.task_id} - ${it.task.task_title}")
                    Log.v(tag, "Number of subtasks: ${it.subtaskList.size}")
                    it.subtaskList.forEach {
                        Log.v(tag, "${it.subtask_id} - ${it.subtask_title}")
                    }

                    Log.v(
                        tag,
                        "-----------------------------------------------------------------------"
                    )
                    count++
                }
            }
        }

    }

}