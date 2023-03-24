package com.pixellore.checklist.utils

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.R

class MultipurposeAlertDialogFragment(
    private val onPosButtonPress: () -> Unit
) : DialogFragment() {

    private lateinit var baseActivityListener: BaseActivityListener

    fun setBaseActivityListener(listener: BaseActivityListener) {
        this.baseActivityListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_multipurpose_alert_dialog, container, false)
    }

    companion object {
        private const val ARG_HEADLINE = "headline"
        private const val ARG_TEXT = "text"
        private const val ARG_POS_BUTTON_TEXT = "ok_button_text"
        private const val ARG_NEG_BUTTON_TEXT = "cancel_button_text"

        fun newInstance(headline: String, text: String,
                        posButtonText: String?, negButtonText: String?,
                        onPosButtonPress: () -> Unit)
        : MultipurposeAlertDialogFragment {
            val args = Bundle().apply {
                putString(ARG_HEADLINE, headline)
                putString(ARG_TEXT, text)
                putString(ARG_POS_BUTTON_TEXT, posButtonText)
                putString(ARG_NEG_BUTTON_TEXT, negButtonText)
            }

            val fragment = MultipurposeAlertDialogFragment(onPosButtonPress)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layout = view.findViewById<ConstraintLayout>(R.id.dialog_layout)
        val headline = view.findViewById<TextView>(R.id.headline)
        val supportingText = view.findViewById<TextView>(R.id.supporting_text)
        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val okBtn = view.findViewById<Button>(R.id.ok_button)

        // Initialize the views in the dialog with current app theme colors
        val currentThemeColors = baseActivityListener.getColorsFromTheme(TaskApplication.appTheme)

        // change colors of the fragment views to the selected theme colors
        if (currentThemeColors.containsKey("colorSurface")) {
            currentThemeColors["colorSurface"]?.let { layout.setBackgroundColor(it) }
        }
        if (currentThemeColors.containsKey("colorOnSurface")) {
            currentThemeColors["colorOnSurface"]?.let { headline.setTextColor(it) }
        }
        if (currentThemeColors.containsKey("colorOnSurfaceVariant")) {
            currentThemeColors["colorOnSurfaceVariant"]?.let { supportingText.setTextColor(it) }
        }
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { okBtn.setTextColor(it) }
            currentThemeColors["colorPrimary"]?.let { cancelBtn.setTextColor(it) }
        }


        // Set the values of the views using the input arguments
        headline.text = arguments?.getString(ARG_HEADLINE)
        if (!arguments?.getString(ARG_TEXT).equals("")){
            supportingText.visibility = View.VISIBLE
            supportingText.text = arguments?.getString(ARG_TEXT)
        } else{
            supportingText.visibility = View.GONE
        }
        //arguments?.getInt(ARG_ICON)?.let { iconImageView.setImageResource(it) }
        okBtn.text = arguments?.getString(ARG_POS_BUTTON_TEXT)
        if (!arguments?.getString(ARG_NEG_BUTTON_TEXT).equals("")){
            cancelBtn.visibility = View.VISIBLE
            cancelBtn.text = arguments?.getString(ARG_NEG_BUTTON_TEXT)
        } else {
            cancelBtn.visibility = View.GONE
        }


        // Setup Buttons
        cancelBtn.setOnClickListener {
            dismiss()
        }

        okBtn.setOnClickListener {
            dismiss()
            onPosButtonPress()
        }

    }
}