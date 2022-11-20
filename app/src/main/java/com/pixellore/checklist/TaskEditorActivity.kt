package com.pixellore.checklist

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText

class TaskEditorActivity : AppCompatActivity() {

    private lateinit var editTaskTitleView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_editor)

        editTaskTitleView = findViewById(R.id.edit_task_title)

        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTaskTitleView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val word = editTaskTitleView.text.toString()
                replyIntent.putExtra(EXTRA_REPLY, word)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }
    companion object {
        const val EXTRA_REPLY = "com.pixellore.checklist.REPLY"
    }
}