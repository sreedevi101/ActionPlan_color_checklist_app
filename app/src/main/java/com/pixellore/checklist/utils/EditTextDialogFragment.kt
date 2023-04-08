package com.pixellore.checklist.utils


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pixellore.checklist.R

/**
 * A dialog fragment to rename the 'Checklist'. This includes an EditText for the user to
 * enter the new name
 *
 * Initially the EditText displays the current name which is passed to this DialogFragment
 * class as input
 *
 * Also a callback function is passed as an input to the class. This function will be called when
 * user presses "Save" button
 * */
class EditTextDialogFragment(
    private val currentName: String?,
    private val onSaveButtonPressed: (newName: String) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userInputLayout = view.findViewById<TextInputLayout>(R.id.user_input_text_layout)
        val userInput = view.findViewById<TextInputEditText>(R.id.user_input_text)
        val cancelBtn = view.findViewById<Button>(R.id.rename_cancel_button)
        val saveBtn = view.findViewById<Button>(R.id.new_name_save_button)

        // set current name in edit text initially
        if (currentName != null){
            userInput.setText(currentName)
        }

        // Setup Buttons
        cancelBtn.setOnClickListener {
            dismiss()
        }

        saveBtn.setOnClickListener {
            val newName = userInput.text.toString()
            if (newName.isNotEmpty()){
                dismiss()
                onSaveButtonPressed(newName)
            } else {
                // warn user that a checklist name cannot be empty
                userInputLayout.error = "Checklist name cannot be empty"
            }
        }

    }
}