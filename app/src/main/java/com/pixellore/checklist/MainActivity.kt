package com.pixellore.checklist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.AdapterUtility.ChecklistRecycleAdapter
import com.pixellore.checklist.DataClass.Font
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.util.HashMap


class MainActivity : BaseActivity() {

    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    /*
    * List of checklist Ids obtained as LiveData
    * This is used to find out the unique ID to be assigned to the next checklist item
    * */
    private var checklistIds: List<Int> = emptyList()

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_main)

        // Update database ids
        var sequencingIdsFlag: Boolean
        var taskIdList = mutableListOf<Int>()
        var subtaskIdList = mutableListOf<Int>()
        var taskWithSubtasksList = mutableListOf<TaskWithSubtasks>()

        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.main_activity_toolbar)
        setSupportActionBar(toolbar)

        // Setup the recycler view to display the checklists in database
        val checklistRecyclerView = findViewById<RecyclerView>(R.id.checklist_recycler_view)
        val checklistAdapter = ChecklistRecycleAdapter { position, checklist, actionRequested ->
            onListChecklistClick(
                position,
                checklist,
                actionRequested
            )
        }
        checklistRecyclerView.adapter = checklistAdapter
        checklistRecyclerView.layoutManager = LinearLayoutManager(this)

        actionPlanViewModel.allChecklists.observe(this) { checklists ->
            // Update the cached copy of the tasks in the adapter.
            checklists.let {
                checklistAdapter.submitList(it)
            }
        }



        actionPlanViewModel.allTasksWithSubtasks.observe(this) { tasks ->
            taskWithSubtasksList = tasks as MutableList<TaskWithSubtasks>
            //runBlocking { sequence(checklistIds, taskWithSubtasksList, subtaskIdList) }

        }


        // Update the list of IDs
        actionPlanViewModel.getAllSubtaskIds().observe(this) { ids ->
            // save the latest list of ids
            subtaskIdList = ids as MutableList<Int>

            //runBlocking { sequence(checklistIds, taskWithSubtasksList, subtaskIdList) }
        }

        // Observe the list of IDs
        actionPlanViewModel.getAllChecklistIds().observe(this) { ids ->
            // Update the list of IDs
            checklistIds = ids

            //runBlocking { sequence(checklistIds, taskWithSubtasksList, subtaskIdList) }
        }



        // set toolbar background color to colorPrimary of the current theme
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { toolbar.setBackgroundColor(it) }
        }
        // set toolbar title text color to colorOnPrimary of the current theme
        if (currentThemeColors.containsKey("colorOnPrimary")) {
            currentThemeColors["colorOnPrimary"]?.let { toolbar.setTitleTextColor(it) }
        }

        val highlightQuickAddBar = findViewById<LinearLayout>(R.id.quick_add_layout_outside)
        val insideQuickAddBar = findViewById<LinearLayout>(R.id.quick_add_layout_inside)
        val checklistTitleEditText = findViewById<EditText>(R.id.checklist_title)

        currentThemeColors["colorPrimary"]?.let { highlightQuickAddBar.setBackgroundColor(it) }
        currentThemeColors["colorSecondary"]?.let { insideQuickAddBar.setBackgroundColor(it) }
        currentThemeColors["colorOnSecondary"]?.let { checklistTitleEditText.setTextColor(it) }
        currentThemeColors["colorOnSecondary"]?.let { checklistTitleEditText.setHintTextColor(it) }

        /*
        * override the enter key on the soft keyboard in Android to perform a custom action
        * */
        checklistTitleEditText.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER
            ) {

                // insert the checklist with the user entered title
                val checklistId = findNextId(checklistIds)
                if (checklistId == -1){ // unique ids reached max limit of Int
                    // todo alert dialog saying max limit reached
                } else{
                    val checklistTitle = checklistTitleEditText.text.toString()

                    // todo add created on date
                    val newChecklist = Checklist(
                        checklistId, checklistTitle,
                        null, false, null, false,
                        Font(null, null)
                    )

                    Log.v(Constants.TAG, "inserting new checklist")
                    actionPlanViewModel.insertChecklist(newChecklist)

                    // Todo update recycler view to show the new checklist
                    checklistAdapter.notifyItemChanged(checklistId)

                    // clear the text from edit text
                    checklistTitleEditText.text.clear()

                    return@setOnEditorActionListener true
                }
            }
            false
        }

    }

   /* private suspend fun sequence(idsChecklist: List<Int>,
                                 taskWithSubtasksList: List<TaskWithSubtasks>,
                                 idsSubtask: List<Int>){

        // get all task IDs
        val idsTask = mutableListOf<Int>()
        taskWithSubtasksList.forEach {
            idsTask.add(it.task.task_id)
        }

        Log.v(Constants.TAG, "checklist: $idsChecklist task: $idsTask subtask: $idsSubtask")

        if (isIdSequencingRequired(idsChecklist)){ // Checklists
            Log.v(Constants.TAG, "checklist")

            // get new IDs to replace existing IDs
            val newIds = calculateNewIds(idsTask)

            idsChecklist.zip(newIds).forEach checklistIdUpdateLoop@{ (currentId, newId) ->
                if (currentId != newId) {
                    // get the tasks in this checklist
                    // i.e., task_ids with this checklist_id as parent_checklist_id
                    val correspondingTaskIdsList =
                        actionPlanViewModel.getTaskIdsByChecklistId(currentId).toList().flatten()

                    // verify that tasks with these ids exists in database before updating
                    var allTasksExists = true
                    correspondingTaskIdsList.forEach { childTaskId ->
                        val task = actionPlanViewModel.getTaskById(childTaskId)
                        if (task != null) { // task exists

                        } else {
                            // task with this id does not exist, discard further update
                            allTasksExists = false
                        }
                    }

                    if (allTasksExists) {
                        // update task Ids
                        actionPlanViewModel.updateChecklistId(currentId, newId)

                        correspondingTaskIdsList.forEach { childTaskId ->

                            Log.v(
                                Constants.TAG, "Task Parent ID - id: $childTaskId " +
                                        "current: $currentId new: $newId"
                            )
                            // update task parent Id
                            actionPlanViewModel.updateTaskParentChecklistId(
                                childTaskId,
                                currentId,
                                newId
                            )

                        }
                    } else {
                        // break the loop
                        return@checklistIdUpdateLoop
                    }

                }
            }



        } else if (isIdSequencingRequired(idsTask)) { // Tasks
            Log.v(Constants.TAG, "tasks")

            // create a map of task id and list of the corresponding subtask ids
            val taskSubtaskIdMap = mutableMapOf<String, MutableList<Int>>()
            taskWithSubtasksList.forEach {
                val subtasksIds = mutableListOf<Int>()
                it.subtaskList.forEach { subtask -> subtasksIds.add(subtask.subtask_id) }
                taskSubtaskIdMap[it.task.task_id.toString()] = subtasksIds
            }


            // get new IDs to replace existing IDs
            val newIds = calculateNewIds(idsTask)

            Log.v(Constants.TAG, "needs Task ID update - current: $idsTask new: $newIds")

            idsTask.zip(newIds).forEach taskIdUpdateLoop@{ (currentId, newId) ->
                if (currentId != newId) {
                    Log.v(Constants.TAG, "Task ID - current: $currentId new: $newId")

//                    * Check if all the subtasks with 'subtaskId' exists
//                    *
//                    * There is a situation where the subtask Ids might have updated as part of sequencing
//                    * but the changes are not yet reflected in LiveData List<TaskWithSubtasks>. This will
//                    * result in failure to update 'parent_task_id' of subtasks, but task ids might get changed
//                    * resulting in orphaned subtasks with parent_task_id pointing to a task_id that no longer exists
//                    *

                    // check if this subtask exists in database

                    // flag to indicate if all the subtasks with the mentioned subtask_ids
                    // (associated with task_id = currentId) actually exist
                    var allSubtasksExists = true
                    taskSubtaskIdMap[currentId.toString()]?.forEach { subtaskId ->

                        val subtask = actionPlanViewModel.getSubtaskById(subtaskId)
                        if (subtask != null) { // subtask exists

                        } else {
                            // subtask with this id does not exist, discard further update
                            allSubtasksExists = false
                        }

                    }

                    if (allSubtasksExists) {
                        // update task Ids
                        actionPlanViewModel.updateTaskId(currentId, newId)

                        taskSubtaskIdMap[currentId.toString()]?.forEach { subtaskId ->

                            Log.v(
                                Constants.TAG, "Subtask Parent ID - id: $subtaskId " +
                                        "current: $currentId new: $newId"
                            )
                            // update subtask parent Id
                            actionPlanViewModel.updateSubtaskParentTaskId(
                                subtaskId,
                                currentId,
                                newId
                            )

                        }
                    } else {
                        // break the loop
                        return@taskIdUpdateLoop
                    }

                }
            }
        } else if (isIdSequencingRequired(idsSubtask)){ // Subtasks
            Log.v(Constants.TAG, "subtasks")

            // get new IDs to replace existing IDs
            val newIds = calculateNewIds(idsSubtask)

            Log.v(Constants.TAG, "needs Subtask ID update - current: $idsSubtask new: $newIds")

            idsSubtask.zip(newIds).forEach { (currentId, newId) ->
                if (currentId != newId){
                    Log.v(Constants.TAG, "Subtask ID - current: $currentId new: $newId")
                    // update subtask Ids
                    actionPlanViewModel.updateSubtaskId(currentId, newId)
                }
            }
        }
    }
*/

    /*
    * This function is to support Sequencing of IDs
    * check if the ids are out of sequence, return false if IDs are in sequence (no sequencing required),
    * return true if sequencing required (Ids not in sequence)
    * */
/*
    private fun isIdSequencingRequired(ids: List<Int>):Boolean{
        var flag = false
        if (ids.isNotEmpty()) {
            val highestId = ids.maxOrNull() ?: 0
            val newIdsList = (1..highestId).toList().take(ids.size) // this is how ids should be without gaps

            if (newIdsList != ids) {
                flag = true
            }
        }
        return flag
    }
*/

    /*
    * This function is to support Sequencing of IDs
    * make a new list of IDs in sequence
    * */
/*
    private fun calculateNewIds(ids: List<Int>):List<Int>{

        val highestId = ids.maxOfOrNull { it } ?: 0
        val newIdsList = (1..highestId).toList().take(ids.size) // this is how ids should be without gaps

        return newIdsList
    }
*/

    private fun onListChecklistClick(
        position: Int,
        checklist: Checklist,
        actionRequested: Int
    ) {

        if (actionRequested == Constants.UPDATE_DB) {
            actionPlanViewModel.updateChecklist(checklist)
        } else if (actionRequested == Constants.OPEN_EDITOR) {
            // open ChecklistActivity
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("Checklist", checklist)
            startActivity(intent)
        } else if (actionRequested == Constants.DELETE) {

            Log.v(Constants.TAG, "Deleting...")
            // todo alert dialog

            // delete this checklist
            actionPlanViewModel.deleteChecklist(checklist)

            // delete tasks and subtasks in this checklist

            // Get the LiveData object
            val tasksLiveData =
                actionPlanViewModel.allTasksWithSubtasksByChecklistId(checklist.checklist_id)

            // Get the value of the LiveData object
            //val tasksWithSubtasksList = tasksLiveData.value

            // Observe the LiveData object to get the updated value
            tasksLiveData.observe(this) { tasksWithSubtasksList ->
                // Get the task ids
                val taskIds = tasksWithSubtasksList?.map { it.task.task_id }

                // Get the list of subtask ids for each task
                val subtaskIds =
                    tasksWithSubtasksList.flatMap { taskWithSubtasks: TaskWithSubtasks ->
                        taskWithSubtasks.subtaskList.map { subtask: Subtask -> subtask.subtask_id }
                    }


                // delete all tasks in this checklist using their IDs
                Log.v(Constants.TAG, "Deleting taskIds $taskIds") // DEBUG
                if (taskIds != null) {
                    actionPlanViewModel.deleteTasks(taskIds)
                }

                // delete all associated subtasks using their IDs
                Log.v(Constants.TAG, "Deleting subtaskIds $subtaskIds") // DEBUG
                if (subtaskIds != null) {
                    actionPlanViewModel.deleteSubtasks(subtaskIds)
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_delete_all -> {
            // User chose "Delete all", delete database

            // todo alert dialog
            this.deleteDatabase("action_plan_database")
            //todo restart app
            true
        }

        R.id.action_settings -> {
            // User chose the "Settings" item, show the settings UI for the checklist

            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_print_database -> {

            printDbChecklistTable()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }


    override fun onResume() {
        super.onResume()
        setTheme()
        if (TaskApplication.recreateMainActivity) {
            recreate()
            TaskApplication.recreateMainActivity = false
        }
    }

    override fun onStart() {
        super.onStart()
        setTheme()
        if (TaskApplication.recreateMainActivity) {
            recreate()
            TaskApplication.recreateMainActivity = false
        }
    }

    private fun printDbChecklistTable() {

        var count: Int

        actionPlanViewModel.allChecklists.observe(this) { checklists ->
            val numberOfChecklistItems = checklists.size
            Log.v(Constants.TAG, "There are $numberOfChecklistItems checklists")

            checklists.let {
                count = 1
                it.forEach {
                    if (count == 1) {
                        Log.v(
                            Constants.TAG,
                            "\n\n-----------------------------------------------------------------------"
                        )
                    }
                    Log.v(Constants.TAG, "${it.checklist_id}")
                    if (it.checklist_title != null) {
                        Log.v(Constants.TAG, "${it.checklist_title}")
                    } else {
                        Log.v(Constants.TAG, "title is null")
                    }

                    if (it.created_on != null) {
                        Log.v(Constants.TAG, "Created Date: ${it.created_on}")
                    } else {
                        Log.v(Constants.TAG, "created date is null")
                    }

                    Log.v(Constants.TAG, "Is closed: ${it.checklist_isClosed}")

                    if (it.closed_on != null) {
                        Log.v(Constants.TAG, "Closed date: ${it.closed_on}")
                    } else {
                        Log.v(Constants.TAG, "closed date is null")
                    }

                    Log.v(Constants.TAG, "Is pinned: ${it.isPinned}")

                    if (it.font != null) {
                        Log.v(
                            Constants.TAG,
                            "Font: ${it.font!!.backgroundColorResId}, ${it.font!!.textColorResId}"
                        )
                    } else {
                        Log.v(Constants.TAG, "Font is null")
                    }

                    Log.v(
                        Constants.TAG,
                        "-----------------------------------------------------------------------"
                    )
                    count++
                }
            }
        }
    }
}