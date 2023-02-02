package com.pixellore.checklist

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.utils.BaseActivity
import com.pixellore.checklist.utils.Constants

class SettingsActivity : BaseActivity(), ThemePickerDialogFragment.ThemeSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_settings)


        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val themeChangeLayout = findViewById<LinearLayout>(R.id.theme_change_layout)
        themeChangeLayout.setOnClickListener {
            // Open DialogFragment
            val themeSelector: ThemePickerDialogFragment = ThemePickerDialogFragment()
            themeSelector.setListener(this)
            themeSelector.show(supportFragmentManager, "theme_select")
        }
    }

    override fun switchToNewTheme(selectedTheme: Int) {
        // call method in BaseActivity to switch theme to new theme selected
        switchTheme(selectedTheme)
        setTheme()
        recreate()

        // Set flag to recreate other activities
        TaskApplication.recreateMainActivity = true
        TaskApplication.recreateTaskEditor = true
    }
}