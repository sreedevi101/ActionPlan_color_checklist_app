package com.pixellore.checklist

import android.R.string
import android.R.layout
import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pixellore.checklist.AdapterUtility.TaskRecycleAdapter
import com.pixellore.checklist.DatabaseUtility.*
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants


class MainActivity : BaseActivity() {

    private val TAG = "Debug"
    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }
    private var taskListSize: Int = 0
    private var subtaskListSize: Int = 0


    // Receiver
    private val getItemAddActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val taskId = taskListSize + 1
                var newTaskTitle = ""
                var newTaskDetails = ""
                var newTaskDueDate = ""
                var newTaskSubtaskList: Array<String>? = null
                it.data?.getStringExtra(TaskEditorActivity.TASK_TITLE)
                    ?.let { reply -> newTaskTitle = reply }
                it.data?.getStringExtra(TaskEditorActivity.TASK_DETAILS)
                    ?.let { reply -> newTaskDetails = reply }
                it.data?.getStringExtra(TaskEditorActivity.DUE_DATE)
                    ?.let { reply -> newTaskDueDate = reply }
                it.data?.getStringArrayExtra(TaskEditorActivity.SUBTASK_LIST)
                    ?.let { reply -> newTaskSubtaskList = reply }

                Log.v(TAG, "Task ID: $taskId")
                Log.v(
                    TAG,
                    "Title: $newTaskTitle\nDetails: $newTaskDetails\nDueDate: $newTaskDueDate"
                )

                newTaskSubtaskList?.forEach { Log.v(TAG, "MainActivity Subtasks : " + it) }


                // insert corresponding subtasks
                var subtask: Subtask
                var subtaskId: Int = subtaskListSize + 1
                newTaskSubtaskList?.forEach {
                    subtask = Subtask(
                        parent_task_id = taskId,
                        subtask_title = it, subtask_id = subtaskId
                    )
                    actionPlanViewModel.insertSubtask(subtask)
                    subtaskId++
                }

                // Insert new task
                val task = Task(
                    task_title = newTaskTitle, task_id = taskId,
                    details_note = newTaskDetails, due_date = newTaskDueDate
                )
                actionPlanViewModel.insert(task)


            } else {
                Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG)
                    .show()
            }
        }

    private val getItemEditActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // ToDo
                Toast.makeText(applicationContext, "Update task DEBUG", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Could not update", Toast.LENGTH_LONG).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(Constants.TAG, "onCreate")
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_main)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionListRecyclerView = findViewById<RecyclerView>(R.id.actionListRecyclerView)

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

        actionPlanViewModel.allTasksWithSubtasks.observe(this) { tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                adapter.submitList(it)
            }
        }


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
            val intent = Intent(this@MainActivity, TaskEditorActivity::class.java)
            getItemAddActivityResult.launch(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        Log.v(Constants.TAG, "onResume")
        setTheme()
        if (TaskApplication.recreateMainActivity){
            recreate()
            TaskApplication.recreateMainActivity = false
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(Constants.TAG, "onStart")
        setTheme()
        if (TaskApplication.recreateMainActivity){
            recreate()
            TaskApplication.recreateMainActivity = false
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
            val intent = Intent(this@MainActivity, TaskEditorActivity::class.java)

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

        R.id.action_settings -> {
            // User chose the "Settings" item, show the settings UI for the checklist

            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.action_print_database -> {

            printDbTables()
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