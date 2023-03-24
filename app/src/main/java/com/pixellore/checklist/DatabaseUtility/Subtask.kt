package com.pixellore.checklist.DatabaseUtility

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixellore.checklist.DataClass.CustomStyle

@Entity(tableName = "subtask_table")
data class Subtask(
    @PrimaryKey var subtask_id: Int,
    var subtask_pos_id: Int,
    var parent_task_id: Int,
    var subtask_title: String?,
    var subtask_isCompleted: Boolean = false,
    var subtask_font: CustomStyle?
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Subtask) return false
        return (subtask_id == other.subtask_id) &&
                (subtask_pos_id == other.subtask_pos_id) &&
                (parent_task_id == other.parent_task_id) &&
                (subtask_title == other.subtask_title) &&
                (subtask_isCompleted == other.subtask_isCompleted) &&
                (subtask_font == other.subtask_font)
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),

        // read parcelable class CustomStyle
        if (Build.VERSION.SDK_INT >= 33) {
            parcel.readParcelable(CustomStyle::class.java.classLoader, CustomStyle::class.java)
        }else {
            parcel.readParcelable(CustomStyle::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subtask_id)
        parcel.writeInt(subtask_pos_id)
        parcel.writeInt(parent_task_id)
        parcel.writeString(subtask_title)
        parcel.writeByte(if (subtask_isCompleted) 1 else 0)
        parcel.writeParcelable(subtask_font, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Subtask> {
        override fun createFromParcel(parcel: Parcel): Subtask {
            return Subtask(parcel)
        }

        override fun newArray(size: Int): Array<Subtask?> {
            return arrayOfNulls(size)
        }
    }
}