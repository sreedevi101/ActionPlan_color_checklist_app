package com.pixellore.checklist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val themeChangeLayout = findViewById<LinearLayout>(R.id.theme_change_layout)
        themeChangeLayout.setOnClickListener {
            // Open DialogFragment
            val themeSelector:ThemePickerDialogFragment = ThemePickerDialogFragment()
            themeSelector.show(supportFragmentManager,"theme_select")
        }
    }
}