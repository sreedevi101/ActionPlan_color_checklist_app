package com.pixellore.checklist.DatabaseUtility

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixellore.checklist.DataClass.CustomStyle

/**
 * Checklist table
 * */
@Entity(tableName = "checklist_table")
data class Checklist(
    @PrimaryKey var checklist_id: Int,
    var checklist_pos_id: Int,
    var checklist_title: String?,
    var created_on: String?,
    var checklist_isClosed: Boolean = false,
    var closed_on: String?,

    var isPinned: Boolean = false,
    var font: CustomStyle?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
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
        parcel.writeInt(checklist_id)
        parcel.writeInt(checklist_pos_id)
        parcel.writeString(checklist_title)
        parcel.writeString(created_on)
        parcel.writeByte(if (checklist_isClosed) 1 else 0)
        parcel.writeString(closed_on)
        parcel.writeByte(if (isPinned) 1 else 0)
        parcel.writeParcelable(font, flags)
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
