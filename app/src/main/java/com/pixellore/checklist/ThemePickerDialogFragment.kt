package com.pixellore.checklist

import android.content.res.TypedArray
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
import com.pixellore.checklist.utils.Constants
import java.util.*
import kotlin.collections.ArrayList


class ThemePickerDialogFragment : DialogFragment() {

    // RecyclerView for listing age groups
    private lateinit var themesRecyclerViewList: RecyclerView

    private lateinit var listener: ThemeSelectedListener

    var themesList: ArrayList<Theme> = ArrayList()

    fun setListener(listener: ThemeSelectedListener) {
        this.listener = listener
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
        themesList = getThemesData()

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
        val currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

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
                t.font = Font(textColorResId = currentThemeColors["colorPrimary"],
                    null,null,null)
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


                    val themeColors = getColorsFromTheme(themeSelectedResID)

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
                            t.font = Font(textColorResId = themeColors["colorPrimary"],
                                null,null,null)
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
            listener.switchToNewTheme(themeSelectedResID)
            dismiss()
        }

    }


    fun getThemesData(): ArrayList<Theme> {
        // create Arraylist of Themes data class to be displayed in RecyclerView
        val themesList: ArrayList<Theme> = ArrayList()

        // Theme 1
        themesList.add(
            Theme(
                "Professional",
                R.style.Theme_Checklist_Professional,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_Professional),
                null
            )
        )
        // Theme 2
        themesList.add(
            Theme(
                "Shrine pink",
                R.style.Theme_Checklist_ShrinePink,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_ShrinePink),
                null
            )
        )
        // Theme 3
        themesList.add(
            Theme(
                "Blue Orange",
                R.style.Theme_Checklist_BlueOrange,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_BlueOrange),
                null
            )
        )
        // Theme 4
        themesList.add(
            Theme(
                "Classy",
                R.style.Theme_Checklist_Classy,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_Classy),
                null
            )
        )
        // Theme 5
        themesList.add(
            Theme(
                "Blue Teal",
                R.style.Theme_Checklist_BlueTeal,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_BlueTeal),
                null
            )
        )
        // Theme 6
        themesList.add(
            Theme(
                "Green Orange",
                R.style.Theme_Checklist_GreenOrange,
                is_current_theme = isCurrentTheme(R.style.Theme_Checklist_GreenOrange),
                null
            )
        )

        return themesList

    }

    private fun isCurrentTheme(inputThemeResId: Int): Boolean{

        if (inputThemeResId == TaskApplication.appTheme){
            return true
        }

        return false
    }

    fun getColorsFromTheme(themeResId:Int): HashMap<String, Int> {
        val colors: HashMap<String, Int> = HashMap<String, Int>()


        // The attributes you want retrieved
        val attrs = intArrayOf(
            com.google.android.material.R.attr.colorPrimary,
            com.google.android.material.R.attr.colorPrimaryVariant,
            android.R.attr.titleTextColor,
            com.google.android.material.R.attr.colorSecondary,
            com.google.android.material.R.attr.colorSecondaryVariant,
            com.google.android.material.R.attr.colorOnSecondary
        )

        attrs.let {
            val typedArray = activity?.obtainStyledAttributes(themeResId, attrs)

            val colorPrimaryIndex = 0
            val colorPrimaryVariantIndex = 1
            val colorOnPrimaryIndex = 2
            val colorSecondaryIndex = 3
            val colorSecondaryVariantIndex = 4
            val colorOnSecondaryIndex = 5
            // Fetching the colors defined in your style
            //Primary Colors
            val colorPrimary = typedArray?.getColor(colorPrimaryIndex, Color.BLACK)
            val colorPrimaryVariant = typedArray?.getColor(colorPrimaryVariantIndex, Color.BLACK)
            val colorOnPrimary = typedArray?.getColor(colorOnPrimaryIndex, Color.BLACK)

            // Secondary Colors
            val colorSecondary = typedArray?.getColor(colorSecondaryIndex, Color.BLACK)
            val colorSecondaryVariant =
                typedArray?.getColor(colorSecondaryVariantIndex, Color.BLACK)
            val colorOnSecondary = typedArray?.getColor(colorOnSecondaryIndex, Color.BLACK)

            typedArray?.recycle()

            if (colorPrimary != null) {
                colors["colorPrimary"] = colorPrimary
            }
            if (colorPrimaryVariant != null) {
                colors["colorPrimaryVariant"] = colorPrimaryVariant
            }
            if (colorOnPrimary != null) {
                colors["colorOnPrimary"] = colorOnPrimary
            }

            if (colorSecondary != null) {
                colors["colorSecondary"] = colorSecondary
            }
            if (colorSecondaryVariant != null) {
                colors["colorSecondaryVariant"] = colorSecondaryVariant
            }
            if (colorOnSecondary != null) {
                colors["colorOnSecondary"] = colorOnSecondary
            }

            return colors
        }
    }


    // interface to call the switchTheme method in base activity
    interface ThemeSelectedListener {
        fun switchToNewTheme(selectedTheme: Int)
    }
}