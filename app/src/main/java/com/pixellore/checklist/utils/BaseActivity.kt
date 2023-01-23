package com.pixellore.checklist.utils

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

    // Default Theme
    private var defaultTheme: Int? = null
    // Set current Theme to default theme to initialize
    private var currentTheme: Int? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {

        defaultTheme = R.style.Theme_Checklist_Professional
        currentTheme = defaultTheme

        val sharedPref = this.getSharedPreferences(getString(R.string.shared_preference_file), Context.MODE_PRIVATE)
        currentTheme = sharedPref.getInt(resources.getString(R.string.shared_pref_app_theme_key),
            defaultTheme!!
        )

        Log.v(Constants.TAG, "Current ID to from Shared Preferences: $currentTheme")

        super.onCreate(savedInstanceState, persistentState)
    }

    // this method is to be called just before setContentView() of the MainActivity is called,
    // otherwise the Activity will fall onto using the default App Style
    protected fun setTheme() {
        if (currentTheme == null){
            currentTheme = R.style.Theme_Checklist_Professional
        }
        currentTheme?.let { setTheme(it) }
    }

    // Todo is switchTheme required here since the Themes data class holds this information already
    protected fun switchTheme(newTheme:Int) {

        // Do this only if new theme is different from current theme
        if (newTheme!=currentTheme){
            currentTheme = newTheme
            Log.v(Constants.TAG, "New ID to write to Shared Preferences: $currentTheme")
            // Write New theme to Shared Preferences file
            val sharedPrefToWrite = this.getPreferences(Context.MODE_PRIVATE)
            with (sharedPrefToWrite.edit()) {
                currentTheme?.let { putInt(getString(R.string.shared_pref_app_theme_key), it) }
                apply()
            }
        }
        recreate()
    }

    companion object {
        private const val BLUE_TEAL = R.style.Theme_Checklist_BlueTeal
        private const val BLUE_ORANGE = R.style.Theme_Checklist_BlueOrange
        private const val PROFESSIONAL = R.style.Theme_Checklist_Professional
        private const val CLASSY = R.style.Theme_Checklist_Classy
        private const val SHRINE_PINK = R.style.Theme_Checklist_ShrinePink
        private const val GREEN_ORANGE = R.style.Theme_Checklist_GreenOrange
    }
}