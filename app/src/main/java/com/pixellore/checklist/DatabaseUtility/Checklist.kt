package com.pixellore.checklist.DatabaseUtility

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Checklist table
 * */
@Entity(tableName = "checklist_table")
data class Checklist(
    @PrimaryKey val checklist_id: Int,
    var checklist_title: String?,
    var created_on: String?,
    var checklist_isClosed: Boolean = false,
    var closed_on: String?,
    var isPinned: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(checklist_id)
        parcel.writeString(checklist_title)
        parcel.writeString(created_on)
        parcel.writeByte(if (checklist_isClosed) 1 else 0)
        parcel.writeString(closed_on)
        parcel.writeByte(if (isPinned) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Checklist> {
        override fun createFromParcel(parcel: Parcel): Checklist {
            return Checklist(parcel)
        }

        override fun newArray(size: Int): Array<Checklist?> {
            return arrayOfNulls(size)
        }
    }
}
