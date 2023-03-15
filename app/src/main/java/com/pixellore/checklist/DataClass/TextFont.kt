package com.pixellore.checklist.DataClass

data class TextFont(
    val font_name: String,
    val file_name: String,
    val font_resource_id: Int, // id saved in CustomStyle class of the corresponding Task item
    var is_current_font: Boolean, // if this is the current font of a task item
) {
}