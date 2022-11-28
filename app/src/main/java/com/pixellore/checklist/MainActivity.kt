package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pixellore.checklist.AdapterUtility.TaskRecycleAdapter
import com.pixellore.checklist.DatabaseUtility.*

class MainActivity : AppCompatActivity() {

    private val TAG = "Debug"
    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    // Receiver
    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK){
            val taskId = getListSize()
            var newTaskTitle = ""
            var newTaskDetails = ""
            var newTaskDueDate = ""
            var newTaskSubtaskList: Array<String>? = null
            it.data?.getStringExtra(TaskEditorActivity.TASK_TITLE)?.let { reply -> newTaskTitle=reply }
            it.data?.getStringExtra(TaskEditorActivity.TASK_DETAILS)?.let { reply -> newTaskDetails=reply }
            it.data?.getStringExtra(TaskEditorActivity.DUE_DATE)?.let { reply -> newTaskDueDate=reply }
            it.data?.getStringArrayExtra(TaskEditorActivity.SUBTASK_LIST)?.let { reply -> newTaskSubtaskList=reply }

            Log.v(TAG, "Title: $newTaskTitle\nDetails: $newTaskDetails\nDueDate: $newTaskDueDate")

            newTaskSubtaskList?.forEach { Log.v(TAG, "MainActivity Subtasks : " + it) }


            // insert corresponding subtasks
            //var subtasksToTaskList:MutableList<Subtask> = mutableListOf()
            var subtask:Subtask
            var i:Int = 1
            newTaskSubtaskList?.forEach {
                subtask = Subtask(parent_task_id = taskId,
                    subtask_title = it, subtask_id = i)
                actionPlanViewModel.insertSubtask(subtask)
                //subtasksToTaskList.add(subtask)
                i++
            }

            // Insert new task
            val task = Task(task_title = newTaskTitle, task_id = taskId,
                details_note = newTaskDetails, due_date = newTaskDueDate)
            actionPlanViewModel.insert(task)


        } else {
            Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionListRecyclerView = findViewById<RecyclerView>(R.id.actionListRecyclerView)
        val adapter = TaskRecycleAdapter()
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

        actionPlanViewModel.allTasksWithSubtasks.observe(this){ tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                Log.v("ViewModel", it.size.toString())
                adapter.submitList(it) }
        }

        val fab = findViewById<FloatingActionButton>(R.id.addItemFab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskEditorActivity::class.java)
            getResult.launch(intent)
        }

    }


    private fun getListSize(): Int {
        var listSize: Int = 0
        actionPlanViewModel.allChecklistItems.observe(this) { tasks ->
            // Update the cached copy of the tasks in the adapter.
            tasks.let {
                Log.v("ViewModel", it.size.toString())
                listSize = it.size
            }
        }
        return listSize
    }


}