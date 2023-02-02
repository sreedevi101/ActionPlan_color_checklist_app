package com.pixellore.checklist.utils

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pixellore.checklist.DataClass.Theme
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.R

/*
* This activity class is part of the feature "Theme Selection by User"
*
* Purpose of this class includes:
*  - reading current selected theme from Shared Preferences
*  - function to set the selected theme to the app
*  - function to switch to the theme selected by user
*  - write selected theme to Shared Preference
* */
abstract class BaseActivity : AppCompatActivity() {


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
        Log.v(
            Constants.TAG, "Setting theme to - ID: " +
                    TaskApplication.appTheme + " Name: " + currentThemeName
        )

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

    fun writeToSharedPref() {
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

    // Following methods only for DEBUGGING
    /*
    * THIS METHOD ID FOR DEBUGGING PURPOSE ONLY
    * Get the name of the theme from the resource ID
    * */
    fun getThemeName(id: Int): String {

        themesList = getThemesData()

        for (theme in themesList) {
            if (theme.theme_resource_id == id) {
                return theme.theme_name
            }
        }

        return ""
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


}