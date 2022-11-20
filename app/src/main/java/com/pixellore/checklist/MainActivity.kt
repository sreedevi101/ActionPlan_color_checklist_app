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
import com.pixellore.checklist.AdapterUtility.ActionListRVAdapter
import com.pixellore.checklist.DatabaseUtility.Task
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.DatabaseUtility.ActionPlanViewModel
import com.pixellore.checklist.DatabaseUtility.ActionPlanViewModelFactory

class MainActivity : AppCompatActivity() {

    private val actionItemEditorActivityRequestCode = 1
    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    // Receiver
    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK){
                val value = it.data?.getStringExtra(TaskEditorActivity.EXTRA_REPLY)?.let { reply ->
                    val actionListSize: Int = getListSize()
                    val taskTitle = Task(title = reply, id = actionListSize)
                    //val taskTitle = ActionItem(title = reply)
                    actionPlanViewModel.insert(taskTitle)
                }
            }
        else {
            Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show()
        }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionListRecyclerView = findViewById<RecyclerView>(R.id.actionListRecyclerView)
        val adapter = ActionListRVAdapter()
        actionListRecyclerView.adapter = adapter
        actionListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Add an observer on the LiveData returned by ActionItem.getItem()
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        actionPlanViewModel.allChecklistItems.observe(this) { tasks ->
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