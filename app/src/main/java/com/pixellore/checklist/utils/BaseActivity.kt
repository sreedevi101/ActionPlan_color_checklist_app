package com.pixellore.checklist.utils

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pixellore.checklist.DataClass.TextFont
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.R
import java.util.HashMap

/*
* This activity class is part of the feature "Theme Selection by User"
*
* Purpose of this class includes:
*  - reading current selected theme from Shared Preferences
*  - function to set the selected theme to the app
*  - function to switch to the theme selected by user
*  - write selected theme to Shared Preference
*
*  - Place for functions to be used in multiple activities
* */
abstract class BaseActivity : AppCompatActivity(), BaseActivityListener {


    private var themesList: ArrayList<Theme> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {


        super.onCreate(savedInstanceState, persistentState)
    }


    // this method is to be called just before setContentView() of the MainActivity is called,
    // otherwise the Activity will fall onto using the default App Style
    protected fun setTheme() {

        // set the theme to ResId stored in appTheme
        setTheme(TaskApplication.appTheme)

        val currentThemeName = getThemeName(TaskApplication.appTheme)
        /*Log.v(
            Constants.TAG, "Setting theme to - ID: " +
                    TaskApplication.appTheme + " Name: " + currentThemeName
        )*/

    }


    protected fun switchTheme(newTheme: Int) {

        // DEBUG
        val newThemeName = getThemeName(newTheme)
        Log.v(Constants.TAG, "New Theme - ID: $newTheme, Name: $newThemeName")

        // Do this only if new theme is different from current theme
        if (newTheme != TaskApplication.appTheme) {
            TaskApplication.appTheme = newTheme

            writeToSharedPref()

        } else {
            Log.v(Constants.TAG, "New theme selected is same as current theme")
        }
    }

    private fun writeToSharedPref() {
        // Write New theme to Shared Preferences file
        val sharedPrefToWrite = this.getSharedPreferences(
            resources.getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        with(sharedPrefToWrite.edit()) {
            putInt(getString(R.string.shared_pref_app_theme_key), TaskApplication.appTheme)
            apply()
        }
        Log.v(Constants.TAG, "wrote to shared pref")
    }


    override fun getColorsFromTheme(themeResId:Int): HashMap<String, Int> {
        val colors: HashMap<String, Int> = HashMap<String, Int>()


        // The attributes you want retrieved
        val attrs = intArrayOf(
            com.google.android.material.R.attr.colorPrimary,
            com.google.android.material.R.attr.colorPrimaryVariant,
            android.R.attr.titleTextColor,
            com.google.android.material.R.attr.colorSecondary,
            com.google.android.material.R.attr.colorSecondaryVariant,
            com.google.android.material.R.attr.colorOnSecondary,
            com.google.android.material.R.attr.colorSurface,
            com.google.android.material.R.attr.colorSurfaceVariant,
            com.google.android.material.R.attr.colorOnSurface,
            com.google.android.material.R.attr.colorOnSurfaceVariant,
        )

        attrs.let {
            val typedArray = obtainStyledAttributes(themeResId, attrs)

            val colorPrimaryIndex = 0
            val colorPrimaryVariantIndex = 1
            val colorOnPrimaryIndex = 2
            val colorSecondaryIndex = 3
            val colorSecondaryVariantIndex = 4
            val colorOnSecondaryIndex = 5
            val colorSurfaceIndex = 6
            val colorSurfaceVariantIndex = 7
            val colorOnSurfaceIndex = 8
            val colorOnSurfaceVariantIndex = 9
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

            // Surface colors
            val colorSurface = typedArray?.getColor(colorSurfaceIndex, Color.BLACK)
            val colorSurfaceVariant = typedArray?.getColor(colorSurfaceVariantIndex, Color.BLACK)
            val colorOnSurface = typedArray?.getColor(colorOnSurfaceIndex, Color.BLACK)
            val colorOnSurfaceVariant = typedArray?.getColor(colorOnSurfaceVariantIndex, Color.BLACK)

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
            if (colorSurface != null) {
                colors["colorSurface"] = colorSurface
            }
            if (colorSurfaceVariant != null) {
                colors["colorSurfaceVariant"] = colorSurfaceVariant
            }
            if (colorOnSurface != null) {
                colors["colorOnSurface"] = colorOnSurface
            }
            if (colorOnSurfaceVariant != null) {
                colors["colorOnSurfaceVariant"] = colorOnSurfaceVariant
            }

            return colors
        }
    }


    /*
    * THIS METHOD ID FOR DEBUGGING PURPOSE ONLY
    * Get the name of the theme from the resource ID
    * */
    private fun getThemeName(id: Int): String {

        themesList = getThemesData()

        for (theme in themesList) {
            if (theme.theme_resource_id == id) {
                return theme.theme_name
            }
        }

        return ""
    }


    /*
    * This method provide theme data to be displayed in the theme selection dialog fragment
    * */
    override fun getThemesData(): ArrayList<Theme> {
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

    /**
     * Find the id to be assigned to the new item
     *
     * Given a list of IDs, finds and return the next ID. If the list is empty, returns 1 as the next ID.
     *
     * Function common for Checklist, Task or Subtask being added
     * To be used in multiple activities
     * */
    fun findNextId(ids: List<Int>): Int {
        if (ids.isEmpty()) {
            return 1
        }
        val highestId = ids.maxOrNull() ?: 0

        if (highestId + 1 == Int.MAX_VALUE){
            return -1 // reached max limit
        } else {
            return highestId + 1
        }
    }

    /**
     * return the position id. find the position id based on the unique ids of the items
     *
     * need not be unique so if max value reached, return highest value. Moreover, position id
     * would always be equal to or less than unique id. No checks performed for simplicity
     */
    fun findNextPositionId(uniqueIds: List<Int>): Int {

        if (uniqueIds.isEmpty()) { //if no items
            return 1 // place as first item
        }

        val numberOfItems = uniqueIds.size

        if (numberOfItems + 1 == Int.MAX_VALUE){
            val nextItemPosition = numberOfItems // reached max limit
            return nextItemPosition
        } else {
            val nextItemPosition = numberOfItems + 1
            return nextItemPosition
        }
    }

    private fun getFontAssets() : MutableList<String>{
        val assetManager = this.assets
        val fontPath = "fonts"
        val fontFiles = assetManager.list(fontPath)
        val fontList = mutableListOf<String>()
        fontFiles?.let {
            for (font in it) {
                if (font.endsWith(".ttf") || font.endsWith(".otf")) {
                    fontList.add("$fontPath/$font")
                }
            }
        }

        // Debug
        fontList.forEach { Log.v(Constants.TAG, ""+it) }

        return fontList
    }


    private fun formatFontNamesToDisplay(fontsNamesList:ArrayList<String>) : ArrayList<String>{

        // split each font name and add the resulting parts to a new list
        val fontFamilies: ArrayList<String> = ArrayList()

        for (fontName in fontsNamesList) {
            val nameParts = fontName.split("-", "/")
            if (nameParts.isNotEmpty()) {
                val fontFamily = nameParts[1]
                if (!fontFamilies.contains(fontFamily)) {
                    fontFamilies.add(fontFamily)
                }
            }
        }

        return fontFamilies
    }

    override fun getFontsData(): ArrayList<TextFont> {

        val fontsList: ArrayList<TextFont> = ArrayList()

        val fontNamesList = getFontAssets()

        val fontNamesFormatted = formatFontNamesToDisplay(fontNamesList as ArrayList<String>)


        for (i in 0 until fontNamesFormatted.size) {
            val fontName = fontNamesFormatted[i]
            val fileName = fontNamesList[i]
            val id = i + 1

            val textFont = TextFont(fontName, fileName,
                id, false)
            fontsList.add(textFont)
        }

        return fontsList
    }
}