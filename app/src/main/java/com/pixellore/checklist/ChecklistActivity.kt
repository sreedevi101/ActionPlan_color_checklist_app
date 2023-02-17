package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pixellore.checklist.AdapterUtility.TaskRecycleAdapter
import com.pixellore.checklist.DataClass.Font
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants
import java.io.Serializable
import java.util.HashMap

class ChecklistActivity : BaseActivity() {

    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    /*
    * number of tasks and subtasks - this is to find the unique id when creating the next Task or Subtask
    *
    * Task unique id irrespective of the checklist it belongs to, its unique among all the tasks objects.
    * same about subtasks as well
    *
    * unique id is assigned in the order the object is created
    * */
    private var taskListSize: Int = 0
    private var subtaskListSize: Int = 0

    // checklist object; clicking on this object opens this activity
    private var checklistToDisplay: Checklist? = null

    // RecyclerView
    private lateinit var actionListRecyclerView: RecyclerView

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>


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
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK, Task::class.java)
                } else {
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK)
                }

                /**
                 * Updated Subtask objects array from TaskEditorActivity
                 * */
                val subtasksUpdatedReceivedArray: Array<Subtask?>
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST, Subtask::class.java) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                } else {

                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                }



                // continue to add the Task if only it has a  parent Checklist
                if (checklistToDisplay!=null){

                    // find id to be used for the new task
                    val taskId = taskListSize + 1

                    // get parent checklist id from the Checklist object passed to this intent when opening it
                    val parentChecklistId = checklistToDisplay!!.checklist_id

                    // IMPORTANT STEP
                    // Add correct values for IDs
                    if (taskUpdatedReceived != null) {
                        taskUpdatedReceived.task_id = taskId
                        taskUpdatedReceived.parent_checklist_id = parentChecklistId

                        // Insert new task
                        actionPlanViewModel.insert(taskUpdatedReceived)

                        // insert corresponding subtasks
                        var subtaskId: Int = subtaskListSize + 1
                        subtasksUpdatedReceivedArray.forEach { subtask ->
                            if (subtask != null){
                                // IMPORTANT STEP
                                // Add correct values for IDs
                                subtask.subtask_id = subtaskId
                                subtask.parent_task_id = taskId

                                actionPlanViewModel.insertSubtask(subtask)
                                subtaskId++
                            }
                        }
                    }

                } else {
                    Log.v(Constants.TAG, "Parent Checklist missing. Task cannot be added")

                    Toast.makeText(applicationContext,
                        "Parent Checklist missing. Task cannot be added",
                        Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG)
                    .show()
            }
        }

    // Receiver for data from TaskEditorActivity - if TaskEditorActivity is opened to edit existing task
    private val getItemEditActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // ToDo
                Toast.makeText(applicationContext, "Update task DEBUG", Toast.LENGTH_LONG).show()

                // Receive task object and array of subtask objects from editor activity

                /**
                 * Updated Task object from TaskEditorActivity
                 * */
                val taskUpdatedReceived: Task?
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK, Task::class.java)
                } else {
                    taskUpdatedReceived = it.data?.getParcelableExtra(TaskEditorActivity.TASK)
                }

                /**
                 * Updated Subtask objects array from TaskEditorActivity
                 * */
                val subtasksUpdatedReceivedArray: Array<Subtask?>
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST, Subtask::class.java) ?: ArrayList()
                    subtasksUpdatedReceivedArray = subtasksUpdatedList.toTypedArray()
                } else {

                    val subtasksUpdatedList = it.data?.getParcelableArrayListExtra<Subtask>(
                        TaskEditorActivity.SUBTASK_LIST) ?: ArrayList()
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
                    subtaskLabelList = it.data?.getSerializableExtra(TaskEditorActivity.SUBTASK_LABEL,
                        ArrayList::class.java) as ArrayList<MutableMap<String, Any>>?

                } else {
                    // Use Serializable
                    subtaskLabelList = it.data?.getSerializableExtra(TaskEditorActivity.SUBTASK_LABEL)
                            as ArrayList<MutableMap<String, Any>>?
                }


                // DEBUG
                if (subtaskLabelList == null){
                    Log.v(Constants.TAG, "Subtasks list received is NULL")
                } else if (subtaskLabelList.isEmpty()){
                    Log.v(Constants.TAG, "Subtasks list received is EMPTY")
                }


                // continue to add the Task if only it has a  parent Checklist
                if (checklistToDisplay!=null){

                    if (taskUpdatedReceived != null) {

                        // Update new task
                        actionPlanViewModel.update(taskUpdatedReceived)


                        // Update/Insert corresponding subtasks - Delete is separate

                        // subtask id for newly added subtasks (to be inserted into database)
                        var subtaskId: Int = subtaskListSize + 1

                        // iterate through all the subtasks received from TaskEditorActivity
                        subtasksUpdatedReceivedArray.forEach { subtask ->
                            if (subtask != null){

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
                                if (!isIdMatched!!){

                                    // IMPORTANT STEP
                                    // Add correct values for IDs
                                    subtask.subtask_id = subtaskId
                                    subtask.parent_task_id = taskId

                                    actionPlanViewModel.insertSubtask(subtask)
                                    subtaskId++
                                } else {
                                    // if 'isIdMatched' is true, the id is already available, meaning its an
                                    // update/ delete or no change

                                    // get the "label"
                                    val matchedDict = subtaskLabelList.find { dict ->
                                        dict["id"] == subtask.subtask_id
                                    }
                                    val label = matchedDict?.get("label")

                                    when(label){
                                        "UPDATE"-> actionPlanViewModel.updateSubtask(subtask)
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



                        deleteIds?.forEach { idToDelete ->
                            // DEBUG

                            val subtaskListLiveDb = actionPlanViewModel.allChecklistSubtasks.value
                            val subtaskToDelete = subtaskListLiveDb?.find { it.subtask_id == idToDelete }

                            if (subtaskToDelete != null) {
                                actionPlanViewModel.deleteSubtask(subtaskToDelete)

                                /*Log.v(Constants.TAG, "Subtask deleted: " + subtaskToDelete.subtask_id +
                                        " - " + subtaskToDelete.subtask_title)*/
                            }
                        }
                    }

                } else {
                    Log.v(Constants.TAG, "Parent Checklist missing. Task cannot be added")

                    Toast.makeText(applicationContext,
                        "Parent Checklist missing. Task cannot be added",
                        Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, "Could not update", Toast.LENGTH_LONG).show()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_checklist)

        // get data from calling activity
        if (Build.VERSION.SDK_INT >= 33) {
            checklistToDisplay = intent.getParcelableExtra("Checklist", Checklist::class.java)
        }else {
            checklistToDisplay = intent.getParcelableExtra<Checklist>("Checklist")
        }

        Log.v(Constants.TAG, "Opening Checklist: " + (checklistToDisplay?.checklist_title ?: "Checklist is null"))

        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = checklistToDisplay?.checklist_title ?: "Checklist"


        // set toolbar background color to colorPrimary of the current theme
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { toolbar.setBackgroundColor(it) }
        }
        // set toolbar title text color to colorOnPrimary of the current theme
        if (currentThemeColors.containsKey("colorOnPrimary")) {
            currentThemeColors["colorOnPrimary"]?.let { toolbar.setTitleTextColor(it) }
        }

        actionListRecyclerView = findViewById<RecyclerView>(R.id.actionListRecyclerView)

        // change background color
        val bgColor = checklistToDisplay?.font?.backgroundColorResId
        if (bgColor != null){
            actionListRecyclerView.setBackgroundColor(bgColor)
        }

        /*
        * This is to remove the flickering of items in the recycler view when the item is updated
        * */
        actionListRecyclerView.itemAnimator?.changeDuration = 0


        val adapter = TaskRecycleAdapter(
            { position, taskWithSubtasks, actionRequested ->
                onListItemClick(
                    position,
                    taskWithSubtasks,
                    actionRequested
                )
            },
            { position, subtask -> onListSubtaskClick(position, subtask) })


        actionListRecyclerView.adapter = adapter
        actionListRecyclerView.layoutManager = LinearLayoutManager(this)


        // Add an observer on the LiveData returned by ActionItem.getItem()
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        /*actionPlanViewModel.allChecklistItems.observe(this) { tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                Log.v("ViewModel", it.size.toString())
                adapter.submitList(it) }
        }*/




        /*actionPlanViewModel.allTasksWithSubtasks.observe(this) { tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                adapter.submitList(it)
            }
        }*/

        // get only TasksWithSubtasks belongs to this checklist
        if (checklistToDisplay!=null){
            actionPlanViewModel.allTasksWithSubtasksByChecklistId(checklistToDisplay!!.checklist_id).observe(this) { tasks ->
                // Update the cached copy of the tasks in the adapter.
                tasks.let {
                    adapter.submitList(it)
                }
            }
        }
        // todo add an else condition


        actionPlanViewModel.allChecklistTasks.observe(this) { tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                Log.v("ViewModel", it.size.toString())
                taskListSize = it.size
            }
        }



        actionPlanViewModel.allChecklistSubtasks.observe(this) { subtasks ->
            subtasks.let {
                subtaskListSize = it.size
            }
        }


        val fab = findViewById<FloatingActionButton>(R.id.addItemFab)
        fab.setOnClickListener {
            val intent = Intent(this@ChecklistActivity, TaskEditorActivity::class.java)
            getItemAddActivityResult.launch(intent)
        }

    }


    override fun onResume() {
        super.onResume()
        setTheme()
        if (TaskApplication.recreateChecklistActivity){
            recreate()
            TaskApplication.recreateChecklistActivity = false
        }
    }

    override fun onStart() {
        super.onStart()
        setTheme()
        if (TaskApplication.recreateChecklistActivity){
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
            actionPlanViewModel.update(taskWithSubtasks.task)
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
        }
    }

    private fun onListSubtaskClick(position: Int, subtask: Subtask) {
        actionPlanViewModel.updateSubtask(subtask)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.checklist_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_delete_all -> {
            // User chose "Delete all", delete all the tasks and subtasks in the checklist
            actionPlanViewModel.deleteAll()
            actionPlanViewModel.deleteAllSubtasks()
            true
        }

        R.id.action_print_database -> {

            printDbTables()
            true
        }

        R.id.action_change_background -> {
            if (checklistToDisplay != null){
                // open color picker
                ColorPickerDialog
                    .Builder(this)        				// Pass Activity Instance
                    .setTitle("Choose Color")           	// Default "Choose Color"
                    .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                    .setDefaultColor(android.R.color.white)     // Pass Default Color
                    .setColorListener { color, colorHex ->
                        // Handle Color Selection
                        //Log.v(Constants.TAG, "Color Selected: $color")
                        //Log.v(Constants.TAG, "Color Selected - Hex: $colorHex")

                        // change background color
                        actionListRecyclerView.setBackgroundColor(color)

                        // modify Checklist item to save in the database
                        if (checklistToDisplay!!.font != null){
                            checklistToDisplay!!.font?.backgroundColorResId = color
                        } else{
                            val font = Font(backgroundColorResId = color)
                            checklistToDisplay!!.font = font
                        }

                        // update in database
                        actionPlanViewModel.updateChecklist(checklistToDisplay!!)
                    }.show()
                }
            else {

                Toast.makeText(applicationContext,
                    "Parent Checklist missing. This feature will not work",
                    Toast.LENGTH_LONG)
                    .show()
            }


            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }

    private fun printDbTables() {
        val tag = "Print Database Tables"
        val tasksTableSize = taskListSize
        var count: Int

        Log.v(tag, "There are $tasksTableSize tasks")
        if (tasksTableSize > 0) {
            actionPlanViewModel.allChecklistTasks.observe(this) { tasks ->
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
                        Log.v(
                            tag,
                            "-----------------------------------------------------------------------"
                        )
                        count++
                    }
                }
            }


            val subtasksTableSize = subtaskListSize
            Log.v(tag, "There are $subtasksTableSize subtasks")
            actionPlanViewModel.allChecklistSubtasks.observe(this) { tasks ->
                tasks.let {
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


}