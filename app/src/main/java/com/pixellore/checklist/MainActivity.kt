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
import java.util.HashMap


class MainActivity : BaseActivity() {

    private val actionPlanViewModel: ActionPlanViewModel by viewModels {
        ActionPlanViewModelFactory((application as TaskApplication).repository)
    }

    private var checklistSize: Int = 0

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


        actionPlanViewModel.allChecklists.observe(this) { checklists ->
            // Update the cached copy of the tasks in the adapter.
            checklists.let {
                Log.v("ViewModel", it.size.toString())
                checklistSize = it.size
            }
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
                val checklistId = checklistSize + 1
                Log.v(Constants.TAG, "checklistSize : " + checklistSize)
                val checklistTitle = checklistTitleEditText.text.toString()

                // todo add created on date
                val newChecklist = Checklist(checklistId, checklistTitle,
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
            false
        }


    }


    private fun onListChecklistClick(
        position: Int,
        checklist: Checklist,
        actionRequested: Int
    ){

        if (actionRequested == Constants.UPDATE_DB){
            actionPlanViewModel.updateChecklist(checklist)
        } else if (actionRequested == Constants.OPEN_EDITOR){
            // open ChecklistActivity
            val intent = Intent(this, ChecklistActivity::class.java)
            intent.putExtra("Checklist",checklist)
            startActivity(intent)
        } else if (actionRequested == Constants.DELETE) {
            actionPlanViewModel.deleteChecklist(checklist)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.action_delete_all -> {
            // User chose "Delete all", delete all Checklists , the tasks and subtasks in the checklists
            actionPlanViewModel.deleteAllChecklists()
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

        val checklistTableSize = checklistSize
        var count: Int

        Log.v(Constants.TAG, "There are $checklistTableSize checklists")
        if (checklistTableSize > 0) {
            actionPlanViewModel.allChecklists.observe(this) { checklists ->
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
                            Log.v(Constants.TAG, "Font: ${it.font!!.backgroundColorResId}, ${it.font!!.textColorResId}")
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
}