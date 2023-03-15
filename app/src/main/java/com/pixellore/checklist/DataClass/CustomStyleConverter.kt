package com.pixellore.checklist.DataClass

import androidx.room.TypeConverter

class CustomStyleConverter {
    @TypeConverter
    fun fromFont(font: CustomStyle?): String {
        if (font == null) return "null"
        return "${font.headingTextColorResId ?: "null"}|${font.bodyTextColorResId ?: "null"}" +
                "|${font.backgroundColorResId ?: "null"}|${font.textFontName ?: "null"}"
    }

    @TypeConverter
    fun toFont(value: String?): CustomStyle? {
        if (value == null) return null
        val parts = value.split("|")
        return CustomStyle(
            if (parts[0] == "null") null else parts[0].toIntOrNull(),
            if (parts[1] == "null") null else parts[1].toIntOrNull(),
            if (parts[2] == "null") null else parts[2].toIntOrNull(),
            if (parts[3] == "null") null else parts[3]
        )
    }
}