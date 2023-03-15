package com.pixellore.checklist.DataClass


/*
* This data class is part of the feature "Theme Selection by User"
*
* This class holds the details about the themes like the name of the theme,
* integer number corresponding the theme (which is used for storing in shared preferences),
* whether or not this theme is selected by user,
* whether or not this is the current theme
* */
data class Theme(
    val theme_name: String,
    val theme_resource_id: Int, // Resource ID of a theme; this number will be saved in Shared Preferences file
    var is_current_theme: Boolean, // if this is the current theme stored in Shared preferences
    var font: CustomStyle?
) {
}