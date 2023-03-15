package com.pixellore.checklist.DatabaseUtility

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixellore.checklist.DataClass.Font


@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey var task_id: Int,
    var task_title: String?,
    var details_note: String? = "",
    var due_date: String? = "",
    var priority: String? = "None",
    var isExpanded: Boolean = false,
    var task_isCompleted: Boolean = false,
    var parent_checklist_id: Int,
    var task_font: Font?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),

        // read parcelable class Font
        if (Build.VERSION.SDK_INT >= 33) {
            parcel.readParcelable(Font::class.java.classLoader, Font::class.java)
        }else {
            parcel.readParcelable(Font::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(task_id)
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