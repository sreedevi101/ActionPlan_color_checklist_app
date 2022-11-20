package com.pixellore.checklist.DatabaseUtility

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/*
*
* You want to have only one instance of the database and of the repository in your app.
* An easy way to achieve this is by creating them as members of the Application class.
* Then they will just be retrieved from the Application whenever they're needed, rather than
*  constructed every time.
* */

class TaskApplication: Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts

    val database by lazy { TaskDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TaskRepository(database.actionItemDao()) }

}