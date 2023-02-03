package com.pixellore.checklist

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.utils.BaseActivity
import java.util.HashMap

class SettingsActivity : BaseActivity(), ThemePickerDialogFragment.ThemeSelectedListener {

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_settings)


        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // set toolbar background color to colorPrimary of the current theme
        if (currentThemeColors.containsKey("colorPrimary")) {
            currentThemeColors["colorPrimary"]?.let { toolbar.setBackgroundColor(it) }
        }
        // set toolbar title text color & back arrow color to colorOnPrimary of the current theme
        if (currentThemeColors.containsKey("colorOnPrimary")) {
            currentThemeColors["colorOnPrimary"]?.let { toolbar.setTitleTextColor(it) }

            // set back arrow color
            val nav = toolbar.navigationIcon
            if (nav!=null){
                currentThemeColors["colorOnPrimary"]?.let { nav.setTint(it) }
            }
        }


        val themeChangeLayout = findViewById<LinearLayout>(R.id.theme_change_layout)
        themeChangeLayout.setOnClickListener {
            // Open DialogFragment
            val themeSelector: ThemePickerDialogFragment = ThemePickerDialogFragment()
            themeSelector.setThemeSelectedListener(this)
            themeSelector.setBaseActivityListener(this)
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