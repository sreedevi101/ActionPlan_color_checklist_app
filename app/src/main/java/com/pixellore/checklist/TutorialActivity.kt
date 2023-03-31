package com.pixellore.checklist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pixellore.checklist.AdapterUtility.TutorialRecyclerAdapter
import com.pixellore.checklist.DataClass.TutorialPage
import com.pixellore.checklist.utils.BaseActivity

class TutorialActivity: BaseActivity() {

    private var tutorialPagesData = mutableListOf<TutorialPage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme() // to set theme to the theme saved in SharedPreference
        setContentView(R.layout.activity_tutorial)

        defineTutorialData()

        val tutorialViewPager = findViewById<ViewPager2>(R.id.tutorial_viewpager)

        val tutorialAdapter = TutorialRecyclerAdapter(tutorialPagesData)

        tutorialViewPager.adapter = tutorialAdapter


        // Tab Layout
        val tabLayout = findViewById<TabLayout>(R.id.page_dots_tab)


        // Connect TabLayout to ViewPager2
        // https://stackoverflow.com/questions/38459309/how-do-you-create-an-android-view-pager-with-a-dots-indicator
        val tabLayoutMediator = TabLayoutMediator(
            tabLayout, tutorialViewPager,
            true
        ) { tab, position -> }
        tabLayoutMediator.attach()

        val doneButton = findViewById<Button>(R.id.done_button)
        val learnMoreButton = findViewById<Button>(R.id.learn_more_button)

        doneButton.setOnClickListener { onDoneButtonClick() }
        learnMoreButton.setOnClickListener { onLearnMoreButtonClick() }

    }



    private fun onDoneButtonClick(){
        onBackPressed()
    }

    private fun onLearnMoreButtonClick(){
        // Open User manual
        // todo change link to user manual
        val userManualLink =
            "https://github.com/sreedevi101/gallery-search-app/blob/main/User%20Guide.md"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(userManualLink))
        startActivity(browserIntent)
    }

    private fun defineTutorialData(){


        tutorialPagesData.add(
            TutorialPage("Add a checklist",
                    " ● Add a checklist by giving a name to the checklist in the checklist" +
                    " quick add drawer at the bottom of Home page.\n\n" +
                            " ● Open the checklist page by clicking on " +
                    "the checklist item created",
            R.drawable.checklist)
        )

        tutorialPagesData.add(TutorialPage("Add tasks",
                    " ● Add a task by clicking '+' at the bottom of the checklist page. " +
                    "\n\n" +
                    " ● Optionally, add details about the task and pick a due date.\n \u25CF Add subtasks to the " +
                    "task by clicking 'Add Subtask' button",
            R.drawable.add_task))


        tutorialPagesData.add(TutorialPage("Change Color",
                    " ● Background color and text color of Checklist, task and task details+subtasks " +
                    "can be set independently.\n\n" +
                            " ● Press the 'More options' icon on each task/checklist item " +
                    "to view the options. \n\n" +
                            " ● Options include - 'Change Background color', 'Change Text color', etc.",
            R.drawable.palette))


        tutorialPagesData.add(TutorialPage("Change Font",
                    " ● Change the text font of task item or checklist item by clicking the option " +
                    "'More options'(three vertical dots on each item)-> 'Change text font'." +
                    "\n\n" +
                            " ● Large list with variety of fonts available to choose from.",
            R.drawable.text_format))


        tutorialPagesData.add(TutorialPage("Clear and apply styles",
                    " ● The color and font styles can be applied to all the tasks in a checklist. " +
                    "\n\n" +
                            " ● Open the popup menu from the checklist page and select option 'Apply Style to all Tasks. " +
                    "\n\n" +
                            " ● For task/checklist item, to clear all the styles applied and go back to default color and font, select " +
                    "'More options'(three vertical dots on each item)-> 'Change to default format'",
            R.drawable.format_paint))

        tutorialPagesData.add(TutorialPage("Move tasks",
                    " ● Tasks in a checklist can be rearranged by click and drag on the task item. " +
                    "\n\n" +
                    " ● Checklists can be pinned to display at the top by clicking the 'pin' icon",
            R.drawable.drag_pan))

    }
}