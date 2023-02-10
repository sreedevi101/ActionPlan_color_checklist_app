package com.pixellore.checklist.DataClass

import androidx.room.TypeConverter

class FontConverter {
    @TypeConverter
    fun fromFont(font: Font?): String {
        if (font == null) return "null"
        return "${font.textColorResId ?: "null"}|${font.backgroundColorResId ?: "null"}"
    }

    @TypeConverter
    fun toFont(value: String?): Font? {
        if (value == null) return null
        val parts = value.split("|")
        return Font(
            if (parts[0] == "null") null else parts[0].toIntOrNull(),
            if (parts[1] == "null") null else parts[1].toIntOrNull()
        )
    }
}