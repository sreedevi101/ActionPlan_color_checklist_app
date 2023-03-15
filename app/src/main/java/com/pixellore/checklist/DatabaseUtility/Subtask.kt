package com.pixellore.checklist.DatabaseUtility

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixellore.checklist.DataClass.Font

@Entity(tableName = "subtask_table")
data class Subtask(
    @PrimaryKey var subtask_id: Int,
    var parent_task_id: Int,
    var subtask_title: String?,
    var subtask_isCompleted: Boolean = false,
    var subtask_font: Font?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),

        // read parcelable class Font
        if (Build.VERSION.SDK_INT >= 33) {
            parcel.readParcelable(Font::class.java.classLoader, Font::class.java)
        }else {
            parcel.readParcelable(Font::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subtask_id)
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