package com.pixellore.checklist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixellore.checklist.AdapterUtility.ThemesRecycleAdapter
import com.pixellore.checklist.DataClass.Font
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.utils.BaseActivityListener
import com.pixellore.checklist.utils.Constants
import java.util.*
import kotlin.collections.ArrayList


class ThemePickerDialogFragment : DialogFragment() {

    // RecyclerView for listing age groups
    private lateinit var themesRecyclerViewList: RecyclerView

    private lateinit var themeSelectedListener: ThemeSelectedListener
    private lateinit var baseActivityListener: BaseActivityListener

    var themesList: ArrayList<Theme> = ArrayList()

    fun setThemeSelectedListener(listener: ThemeSelectedListener) {
        this.themeSelectedListener = listener
    }

    fun setBaseActivityListener(listener: BaseActivityListener) {
        this.baseActivityListener = listener
    }

    // dialog view is created
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Objects.requireNonNull(dialog)?.window!!.requestFeature(Window.FEATURE_NO_TITLE)

        // Show status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        return inflater.inflate(R.layout.fragment_theme_picker_dialog, null, false)
    }

    //dialog view is ready
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layout = view.findViewById<ConstraintLayout>(R.id.dialog_layout)
        layout.setBackgroundColor(resources.getColor(R.color.white))

        // Data input to Recycler View Adapter
        themesList = baseActivityListener.getThemesData()

        // initialize this for current theme
        var themeSelectedResID: Int = TaskApplication.appTheme
        var themeSelectedName: String = ""

        val mockStatusBar = view.findViewById<ConstraintLayout>(R.id.mock_status_bar)
        val mockToolBar = view.findViewById<ConstraintLayout>(R.id.mock_toolbar)
        val mockToolBarHeading = view.findViewById<TextView>(R.id.mock_toolbar_heading)
        val designLine = view.findViewById<LinearLayout>(R.id.secondary_color_design)
        val cancelBtn = view.findViewById<Button>(R.id.cancel_button)
        val okBtn = view.findViewById<Button>(R.id.ok_button)

        // Initialize the views in the dialog with current app theme colors
        val currentThemeColors = baseActivityListener.getColorsFromTheme(TaskApplication.appTheme)

        // change colors of the fragment views to the selected theme colors
        if (currentThemeColors.containsKey("colorPrimaryVariant")) {
            currentThemeColors["colorPrimaryVariant"]?.let { mockStatusBar.setBackgroundColor(it) }
        }
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { mockToolBar.setBackgroundColor(it) }
            currentThemeColors["colorPrimary"]?.let { okBtn.setTextColor(it) }
            currentThemeColors["colorPrimary"]?.let { cancelBtn.setTextColor(it) }
        }
        if (currentThemeColors.containsKey("colorOnPrimary")) {
            currentThemeColors["colorOnPrimary"]?.let { mockToolBarHeading.setTextColor(it) }
        }
        if (currentThemeColors.containsKey("colorSecondary")) {
            currentThemeColors["colorSecondary"]?.let { designLine.setBackgroundColor(it) }
        }
        // Modify data by adding the current theme primary color as the text color
        if (currentThemeColors.containsKey("colorPrimary")){

            for (t in themesList){
                t.font = Font(headingTextColorResId = currentThemeColors["colorPrimary"])
            }
        }


        // initialize and setup RecyclerView
        themesRecyclerViewList = view.findViewById(R.id.themes_recycler_view)
        themesRecyclerViewList.setHasFixedSize(true)
        themesRecyclerViewList.setItemViewCacheSize(20)
        themesRecyclerViewList.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)


        // create ThemeItemSelectListener to listen to click event on items from the RecyclerView Adapter
        val listenerTheme: ThemesRecycleAdapter.ThemeItemSelectListener =
            object : ThemesRecycleAdapter.ThemeItemSelectListener {
                override fun itemClicked(theme: Theme, position: Int) {
                    Log.v(Constants.TAG, "Theme: " + theme.theme_name)
                    // Save the selected theme index number
                    themeSelectedResID = theme.theme_resource_id
                    themeSelectedName = theme.theme_name


                    val themeColors = baseActivityListener.getColorsFromTheme(themeSelectedResID)

                    // change colors of the fragment views to the selected theme colors
                    if (themeColors.containsKey("colorPrimaryVariant")) {
                        themeColors["colorPrimaryVariant"]?.let { mockStatusBar.setBackgroundColor(it) }
                    }
                    if (themeColors.containsKey("colorPrimary")) {
                        themeColors["colorPrimary"]?.let { mockToolBar.setBackgroundColor(it) }
                        themeColors["colorPrimary"]?.let { okBtn.setTextColor(it) }
                        themeColors["colorPrimary"]?.let { cancelBtn.setTextColor(it) }
                    }
                    if (themeColors.containsKey("colorOnPrimary")) {
                        themeColors["colorOnPrimary"]?.let { mockToolBarHeading.setTextColor(it) }
                    }
                    if (themeColors.containsKey("colorSecondary")) {
                        themeColors["colorSecondary"]?.let { designLine.setBackgroundColor(it) }
                    }
                    // Add the primary color of the selected as color (in Font data class) of the theme data
                    // passed to recyclerview adapter
                    if (themeColors.containsKey("colorPrimary")){

                        for (t in themesList){
                            t.font = Font(headingTextColorResId = themeColors["colorPrimary"])
                        }
                    }
                }
            }



        // create AgeGroupAdapter and pass as parameters the agelist and the AgeItemSelectListener
        val adapter: ThemesRecycleAdapter = ThemesRecycleAdapter(themesList, listenerTheme)
        themesRecyclerViewList.adapter = adapter


        // Setup Buttons
        cancelBtn.setOnClickListener {
            dismiss()
        }

        okBtn.setOnClickListener {
            // set theme
            Log.v(Constants.TAG, "User selected theme " + themeSelectedName + " Apply changes")
            themeSelectedListener.switchToNewTheme(themeSelectedResID)
            dismiss()
        }

    }




    // interface to call the switchTheme method in base activity
    interface ThemeSelectedListener {
        fun switchToNewTheme(selectedTheme: Int)
    }
}