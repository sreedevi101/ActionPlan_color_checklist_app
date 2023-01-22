package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.pixellore.checklist.DatabaseUtility.Subtask
import com.pixellore.checklist.DatabaseUtility.Task


class TaskEditorActivity : AppCompatActivity() {

    private val TAG = "Debug"

    private lateinit var editTaskTitleView: EditText
    private lateinit var editTaskDetailsView: EditText
    private lateinit var addDueDateView: EditText

    private lateinit var subtask1: EditText
    private lateinit var subtaskLayout: LinearLayout
    private lateinit var addSubtaskButton: ImageButton

    private lateinit var editorLayout: LinearLayout

    private var subtaskList: Array<String> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_editor)

        editTaskTitleView = findViewById(R.id.edit_task_title)
        editTaskDetailsView = findViewById(R.id.edit_task_details)
        addDueDateView = findViewById(R.id.add_due_date)

        editorLayout = findViewById(R.id.editor_activity_layout)
        // TODO set background color based on theme
        //editorLayout.setBackgroundColor(resources.getColor(android.R.color.holo_purple, theme))

        subtask1 = findViewById(R.id.subtask1)
        subtaskLayout = findViewById(R.id.subtask_layout)
        addSubtaskButton = findViewById(R.id.add_subtask_btn)

        addSubtaskButton.setOnClickListener{
            val subtaskEditText = EditText(this)
            subtaskEditText.hint = getString(R.string.add_subtask)
            subtaskEditText.setTextAppearance(R.style.edittext_task_editor)
            subtaskEditText.setBackgroundColor(resources.getColor(android.R.color.transparent, theme))
            subtaskEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            subtaskLayout.addView(subtaskEditText, subtaskLayout.childCount)
        }

        /**
         * Already existing Task and Subtasks passed to this Intent to edit
         * */
        val taskToEdit: Task?

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
            taskToEdit = intent.getParcelableExtra("Task", Task::class.java)
        } else {
            taskToEdit =intent.getParcelableExtra("Task")
        }


        val subtaskListSizeToEdit = intent.getIntExtra("NoOfSubtasks", 0)

        val subtaskListToEdit = mutableListOf<Subtask?>()
        var eachSubtask: Subtask?
        for (count in 0..subtaskListSizeToEdit) {


            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                eachSubtask = intent.getParcelableExtra("Subtask_$count", Subtask::class.java)
            } else {
                eachSubtask = intent.getParcelableExtra("Subtask_$count")
            }

            subtaskListToEdit.add(eachSubtask)
        }

        /**
         * In edit mode, Set the current Task and Subtask details to text views
         * */
        if (taskToEdit != null) {
            editTaskTitleView.setText(taskToEdit.task_title)
            editTaskDetailsView.setText(taskToEdit.details_note)
            addDueDateView.setText(taskToEdit.due_date)
        }
        if (subtaskListToEdit.isNotEmpty()) {
            // todo: fill subtasks
        }

        /**
         * Already existing Task and Subtasks passed to this Intent to edit
         * */
        val taskToEdit: Task?

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
            taskToEdit = intent.getParcelableExtra("Task", Task::class.java)
        } else {
            taskToEdit =intent.getParcelableExtra("Task")
        }


        val subtaskListSizeToEdit = intent.getIntExtra("NoOfSubtasks", 0)

        val subtaskListToEdit = mutableListOf<Subtask?>()
        var eachSubtask: Subtask?
        for (count in 0..subtaskListSizeToEdit) {


            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) { // TIRAMISU onwards
                eachSubtask = intent.getParcelableExtra("Subtask_$count", Subtask::class.java)
            } else {
                eachSubtask = intent.getParcelableExtra("Subtask_$count")
            }

            subtaskListToEdit.add(eachSubtask)
        }

        /**
         * In edit mode, Set the current Task and Subtask details to text views
         * */
        if (taskToEdit != null) {
            editTaskTitleView.setText(taskToEdit.task_title)
            editTaskDetailsView.setText(taskToEdit.details_note)
            addDueDateView.setText(taskToEdit.due_date)
        }
        if (subtaskListToEdit.isNotEmpty()) {
            // todo: fill subtasks
        }


        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val title = editTaskTitleView.text.toString()
            Log.v(TAG, title)

            val replyIntent = Intent()
            if (TextUtils.isEmpty(title)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val details = editTaskDetailsView.text.toString()
                val dueDate = addDueDateView.text.toString()

                replyIntent.putExtra(TASK_TITLE, title)
                if (details.isNotEmpty()){
                    replyIntent.putExtra(TASK_DETAILS, details)
                }
                if (dueDate.isNotEmpty()){
                    replyIntent.putExtra(DUE_DATE, dueDate)
                }


                val subtaskTitle1 = subtask1.text.toString()


                //TODO print all subtasks
                Log.v(TAG, "Subtitle: $subtaskTitle1")

                // TODO get all subtasks
                if (subtaskTitle1.isNotEmpty()){
                    subtaskList += subtaskTitle1
                }

                subtaskList.forEach { Log.v(TAG, "Subtitle Loop: $it")}


                if (subtaskList.isNotEmpty()){
                    replyIntent.putExtra(SUBTASK_LIST, subtaskList)
                }

                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }
    companion object {
        const val TASK_TITLE = "com.pixellore.checklist.TASK_TITLE"
        const val TASK_DETAILS = "com.pixellore.checklist.TASK_DETAILS"
        const val DUE_DATE = "com.pixellore.checklist.DUE_DATE"
        const val SUBTASK_LIST = "com.pixellore.checklist.SUBTASK_LIST"
    }
}