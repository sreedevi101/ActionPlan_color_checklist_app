# ActionPlan_color_checklist
 A Checklist app with lot of options to customoze. Color and format each task item and checklist. Multiple theme options.
 
 
 ## Development Version 1
 
 Room Database with RecyclerView to display. 
 Open an activity from the FloatingButton to add a task
 
 ![image](https://user-images.githubusercontent.com/15008191/202901854-53b45b7a-f823-47e1-ab8b-9628521b71c9.png)
 
 ## Development version 2
 
 - Nested and expandable recycler view
 - Toggle the visibility of subtasks when  the item card view is clicked
 - Toggle the checkbox status (checked/ not checked) when it is clicked
 - Update the checkbox status and the subtasks visibility (is the card view expanded or not) in the DB so the status persists when the app is closed and opened again.
 
 

[dev_ver_1.2_recording.webm](https://user-images.githubusercontent.com/15008191/206838010-fabfe8e6-1ace-4ec7-a24a-7f34337a2b77.webm)

[dev_ver_1.3_recording.webm](https://user-images.githubusercontent.com/15008191/206838095-26e61a90-67af-49d7-ba55-70ed30aed4c2.webm)




- Subtasks also can be marked as completed. Their status also will persist by updating to DB


[dev_ver_2_recording.webm](https://user-images.githubusercontent.com/15008191/206841708-0a3e50dc-6db0-4349-b705-717569eb725d.webm)


 ## Development version 3 - Dynamically change theme on user selection
 
 In Settings, there is an option to change the theme of the app. User can select one of the themes from a DialogFragment window that has a recycler view with the available Themes. On selecting a theme, a sample of the theme is displayed on the dialog fragment window by changing the various views like a sample status bar, tool bar and a margin to the colors of the theme.
 
 ![image](https://user-images.githubusercontent.com/15008191/216469129-c556a164-795c-41b0-a332-8ad22547e4f1.png)


 
 [dev_ver_3_theme_changing.webm](https://user-images.githubusercontent.com/15008191/216466622-198acfdd-1ac1-4270-941e-df24e7d7bc51.webm)

## Development version 4 - Checklists

From the previous versions where MainActivity was displaying Tasks, in this version MainActivity is modified to display checklists. Tasks belong inside checklists. When a checklist is clicked, the tasks in that checklist will be displayed. Following are the important changes made:

- Task database table modified to have a "parent_checklist_id" column/variable
- Created Checklist database table and wrote methods for query, insert, update and delete for it
- Added TasksWithSubtasks query to return only entries matching an input "parent_checklist_id"
- Created ChecklistActivity and layout to display tasks in each checklist, fill them with the contents of MainActivity and its layout
- Modify MainActivity to display RecyclerView of Checklists and edit text to create new checklist
- Add click listenerr to open Checklist Activity when a checklist item is clicked. Query DB & display tasks belonging to the selected checklist

![image](https://user-images.githubusercontent.com/15008191/217934433-260567cf-eb7b-4a22-9894-02be237a64e6.png)


## Development version 4.1 - Styling Checklists

Background and text color changing options. [ColorPicker](https://github.com/Dhaval2404/ColorPicker) is used for color selection.

![image](https://user-images.githubusercontent.com/15008191/218152595-935e1752-2656-4dec-88f8-3c18ee9159da.png)


## Development version 4.2 - Task item layout change

![image](https://user-images.githubusercontent.com/15008191/219059531-a5745813-d793-4599-9e1b-37d9b4fd3966.png)

## Development version 4.4 - Task item layout change to accomodate "more menu options"

![image](https://user-images.githubusercontent.com/15008191/224891428-69161fd1-c2a5-423f-8efb-33be06662919.png)

