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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_editor)

        editTaskTitleView = findViewById(R.id.edit_task_title)
        editTaskDetailsView = findViewById(R.id.edit_task_details)

        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTaskTitleView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val title = editTaskTitleView.text.toString()
                val details = editTaskDetailsView.text.toString()

                replyIntent.putExtra(TASK_TITLE, title)
                if (details.isNotEmpty()){
                    replyIntent.putExtra(TASK_DETAILS, details)
                }

                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }
    companion object {
        const val TASK_TITLE = "com.pixellore.checklist.TASK_TITLE"
        const val TASK_DETAILS = "com.pixellore.checklist.TASK_DETAILS"
    }
}