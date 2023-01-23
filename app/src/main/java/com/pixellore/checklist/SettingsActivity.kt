package com.pixellore.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.pixellore.checklist.utils.BaseActivity

class SettingsActivity : BaseActivity(), ThemePickerDialogFragment.ThemeSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeChangeLayout = findViewById<LinearLayout>(R.id.theme_change_layout)
        themeChangeLayout.setOnClickListener {
            // Open DialogFragment
            val themeSelector:ThemePickerDialogFragment = ThemePickerDialogFragment()
            themeSelector.setListener(this)
            themeSelector.show(supportFragmentManager,"theme_select")
        }
    }

    override fun switchToNewTheme(selectedTheme: Int) {
        // call method in BaseActivity to switch theme to new theme selected
        switchTheme(selectedTheme)
    }
}