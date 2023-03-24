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
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.AdapterUtility.ChecklistRecycleAdapter
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants
import com.pixellore.checklist.utils.MultipurposeAlertDialogFragment
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

        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.main_activity_toolbar)
        setSupportActionBar(toolbar)

        // Setup the recycler view to display the checklists in database
        val checklistRecyclerView = findViewById<RecyclerView>(R.id.checklist_recycler_view)
        val checklistAdapter = ChecklistRecycleAdapter ({ position, checklist, actionRequested ->
            onListChecklistClick(
                position,
                checklist,
                actionRequested
            )
        }, this)
        checklistRecyclerView.adapter = checklistAdapter
        checklistRecyclerView.layoutManager = LinearLayoutManager(this)

        actionPlanViewModel.allChecklists.observe(this) { checklists ->
            // Update the cached copy of the tasks in the adapter.
            checklists.let {
                checklistAdapter.submitList(it)
            }
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

                    // alert dialog saying reached max limit
                    val myDialog = MultipurposeAlertDialogFragment.newInstance(
                        headline = "Maximum Limit Reached",
                        text = "The maximum limit of ${Int.MAX_VALUE} checklist unique IDs " +
                                "has been reached. You cannot create any more checklists.",
                        posButtonText = "OK",
                        negButtonText = ""
                    ) {

                    }
                    myDialog.setBaseActivityListener(this)
                    myDialog.show(supportFragmentManager, "dialog")

                } else{
                    val checklistTitle = checklistTitleEditText.text.toString()

                    // get position id (where to place the new checklist)
                    val checklistPositionId = findNextPositionId(checklistIds)

                    // todo add created on date
                    val newChecklist = Checklist(
                        checklistId, checklistPositionId, checklistTitle,
                        null, false, null, false,
                        CustomStyle(null, null,
                            null, null)
                    )

                    Log.v(Constants.TAG, "inserting new checklist")
                    actionPlanViewModel.insertChecklist(newChecklist)

                    // update recycler view to show the new checklist
                    checklistAdapter.notifyItemChanged(checklistId)

                    // clear the text from edit text
                    checklistTitleEditText.text.clear()

                    return@setOnEditorActionListener true
                }
            }
            false
        }

    }

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

            // confirm delete action
            val myDialog = MultipurposeAlertDialogFragment.newInstance(
                headline = "Delete checklist?",
                text = "",
                posButtonText = "Delete",
                negButtonText = "Cancel"
            ) {
                // delete this checklist
                Log.v(Constants.TAG, "Deleting ${checklist.checklist_title}")
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

            myDialog.setBaseActivityListener(this)

            myDialog.show(supportFragmentManager, "dialog")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        if (menu != null) {
            MenuCompat.setGroupDividerEnabled(menu, true)
        };
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_delete_all -> {
            // User chose "Delete all", delete database

            //todo alert dialog requesting confirmation to DELETE (warn app will restart)
            this.deleteDatabase("action_plan_database")
            // todo implement restart app
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
                            "CustomStyle: ${it.font!!}"
                        )
                    } else {
                        Log.v(Constants.TAG, "CustomStyle is null")
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