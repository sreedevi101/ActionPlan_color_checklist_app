package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.pixellore.checklist.DatabaseUtility.Task

class TaskEditorActivity : AppCompatActivity() {

    private val TAG = "Debug"

    private lateinit var editTaskTitleView: EditText
    private lateinit var editTaskDetailsView: EditText
    private lateinit var addDueDateView: EditText

    private lateinit var subtask1: EditText
    private lateinit var subtask2: EditText
    private lateinit var subtask3: EditText
    private lateinit var subtask4: EditText

    private var subtaskList: Array<String> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_editor)

        editTaskTitleView = findViewById(R.id.edit_task_title)
        editTaskDetailsView = findViewById(R.id.edit_task_details)
        addDueDateView = findViewById(R.id.add_due_date)

        subtask1 = findViewById(R.id.subtask1)
        subtask2 = findViewById(R.id.subtask2)
        subtask3 = findViewById(R.id.subtask3)
        subtask4 = findViewById(R.id.subtask4)


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
                val subtaskTitle2 = subtask2.text.toString()
                val subtaskTitle3 = subtask3.text.toString()
                val subtaskTitle4 = subtask4.text.toString()


                Log.v(TAG, "Subtitle: $subtaskTitle1, $subtaskTitle2, $subtaskTitle3, $subtaskTitle4")


                if (subtaskTitle1.isNotEmpty()){
                    subtaskList += subtaskTitle1
                }
                if (subtaskTitle2.isNotEmpty()){
                    subtaskList += subtaskTitle2
                }
                if (subtaskTitle3.isNotEmpty()){
                    subtaskList += subtaskTitle3
                }
                if (subtaskTitle4.isNotEmpty()){
                    subtaskList += subtaskTitle4
                }

                subtaskList.forEach { Log.v(TAG, "Subtitle Loop: $it")}


                if (subtaskList.isNotEmpty() == true){
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