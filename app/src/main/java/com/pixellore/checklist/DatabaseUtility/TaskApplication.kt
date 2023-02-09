package com.pixellore.checklist.DatabaseUtility

import android.app.Application
import android.content.Context
import android.util.Log
import com.pixellore.checklist.R
import com.pixellore.checklist.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/*
*
* You want to have only one instance of the database and of the repository in your app.
* An easy way to achieve this is by creating them as members of the Application class.
* Then they will just be retrieved from the Application whenever they're needed, rather than
*  constructed every time.
* */

class TaskApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts

    val database by lazy { TaskDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TaskRepository(database.actionItemDao()) }


    /**
     * Following part is not related to Task/ Database. It is related to the theme of the application
     *
     * App Feature: "Dynamically update the theme of the app during runtime by user selection"
     *
     * Define a Global variable to save the current theme of the app (ResId of the current theme)
     * */

    companion object {
        var appTheme: Int = -1

        // Flag to recreate  activities after theme change
        var recreateMainActivity: Boolean = false
        var recreateChecklistActivity: Boolean = false
        var recreateTaskEditor: Boolean = false
    }


    override fun onCreate() {
        super.onCreate()
        readAppTheme()
    }

    /**
     * Read the app theme from the Shared Pref file
     * */
    fun readAppTheme() {
        Log.v(Constants.TAG, "Reading from shared pref")

        val sharedPref = this.getSharedPreferences(
            getString(R.string.shared_preference_file),
            Context.MODE_PRIVATE
        )
        appTheme = sharedPref.getInt(
            resources.getString(R.string.shared_pref_app_theme_key),
            R.style.Theme_Checklist_Professional
        )
    }
}