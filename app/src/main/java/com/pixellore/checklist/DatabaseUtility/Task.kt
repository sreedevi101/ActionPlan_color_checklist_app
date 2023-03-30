package com.pixellore.checklist.DatabaseUtility

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixellore.checklist.DataClass.CustomStyle
import com.pixellore.checklist.utils.Constants


@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey var task_id: Int,
    var task_pos_id: Int,
    var task_title: String?,
    var details_note: String? = "",
    var due_date: String? = "",
    var priority: String? = "None",
    var isExpanded: Boolean = false,
    var task_isCompleted: Boolean = false,
    var parent_checklist_id: Int,
    var task_font: CustomStyle?
) : Parcelable {

    // Override the equals method
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Task) return false
        return (task_id == other.task_id) &&
                (task_pos_id == other.task_pos_id) &&
                (task_title == other.task_title) &&
                (details_note == other.details_note) &&
                (due_date == other.due_date) &&
                (priority == other.priority) &&
                (isExpanded == other.isExpanded) &&
                (task_isCompleted == other.task_isCompleted) &&
                (parent_checklist_id == other.parent_checklist_id) &&
                (task_font == other.task_font)
    }
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),

        // read parcelable class CustomStyle
        if (Build.VERSION.SDK_INT >= 33) {
            parcel.readParcelable(CustomStyle::class.java.classLoader, CustomStyle::class.java)
        }else {
            parcel.readParcelable(CustomStyle::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(task_id)
        parcel.writeInt(task_pos_id)
        parcel.writeString(task_title)
        parcel.writeString(details_note)
        parcel.writeString(due_date)
        parcel.writeString(priority)
        parcel.writeByte(if (isExpanded) 1 else 0)
        parcel.writeByte(if (task_isCompleted) 1 else 0)
        parcel.writeInt(parent_checklist_id)
        parcel.writeParcelable(task_font, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}