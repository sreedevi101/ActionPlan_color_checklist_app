package com.pixellore.checklist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.pixellore.checklist.DatabaseUtility.TaskApplication
import com.pixellore.checklist.utils.BaseActivity

class AboutActivity : BaseActivity() {

    // colors from current theme
    private lateinit var currentThemeColors: HashMap<String, Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_about)

        // get colors from current theme, so it can applied to the toolbar and popup menu
        currentThemeColors = getColorsFromTheme(TaskApplication.appTheme)

        // set up Toolbar as action bar for the activity
        val toolbar: Toolbar = findViewById(R.id.toolbarAboutActivity)
        setSupportActionBar(toolbar)
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"

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

        //  Set onClick options for each item in About page
        // Introduction

        // Introduction
        val introductionTextView = findViewById<View>(R.id.about_page_introducton) as TextView

        introductionTextView.setOnClickListener { // open tutorial slides
            //When the button is clicked open the activity for the tutorial
            
            val move = Intent(this@AboutActivity, TutorialActivity::class.java)
            startActivity(move)
        }

        // User Notes

        val userNotesTextView = findViewById<View>(R.id.about_user_manual) as TextView

        userNotesTextView.setOnClickListener { // open tutorial slides
            val userManualLink =
                "https://github.com/sreedevi101/ActionPlan_color_checklist_app/blob/main/User%20Guide.md"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(userManualLink))
            startActivity(browserIntent)
        }

        // Privacy policy

        val privacyPolicyTextView = findViewById<View>(R.id.about_page_privacy_policy) as TextView

        privacyPolicyTextView.setOnClickListener { // open link
            val privacyPolicyLink =
                "https://github.com/sreedevi101/ActionPlan_color_checklist_app/blob/main/Privacy%20Policy.md"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyLink))
            startActivity(browserIntent)
        }


        val termsTextView = findViewById<View>(R.id.about_page_terms_conditions) as TextView

        termsTextView.setOnClickListener { // open link
            val termsConditionsLink =
                "https://github.com/sreedevi101/ActionPlan_color_checklist_app/blob/main/Terms%20%26%20Conditions.md"
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(termsConditionsLink))
            startActivity(browserIntent)
        }


        val contactTextView = findViewById<View>(R.id.about_page_contact) as TextView

        contactTextView.setOnClickListener { // send email
            val developerEmailId = "sreedevi.appdev@gmail.com"
            val subject = "ActionPlan Color Checklist"
            val i = Intent(Intent.ACTION_SENDTO)
            //i.setType("text/plain"); // or:
            i.data = Uri.parse("mailto:") // only email apps should handle this
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf(developerEmailId))
            i.putExtra(Intent.EXTRA_SUBJECT, subject)
            try {
                startActivity(i)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@AboutActivity,
                    "There are no email clients installed.", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Navigate back to parent activity
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}