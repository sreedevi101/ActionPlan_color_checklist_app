package com.pixellore.checklist.DataClass

import android.os.Parcel
import android.os.Parcelable
import com.pixellore.checklist.DatabaseUtility.Task

data class CustomStyle(
    var headingTextColorResId: Int? = null,
    var bodyTextColorResId: Int? = null,
    var backgroundColorResId: Int? = null,
    var textFontName: String? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomStyle) return false
        return (headingTextColorResId == other.headingTextColorResId) &&
                (bodyTextColorResId == other.bodyTextColorResId) &&
                (backgroundColorResId == other.backgroundColorResId) &&
                (textFontName == other.textFontName)
    }

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(String::class.java.classLoader) as? String
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(headingTextColorResId)
        parcel.writeValue(bodyTextColorResId)
        parcel.writeValue(backgroundColorResId)
        parcel.writeValue(textFontName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomStyle> {
        override fun createFromParcel(parcel: Parcel): CustomStyle {
            return CustomStyle(parcel)
        }

        override fun newArray(size: Int): Array<CustomStyle?> {
            return arrayOfNulls(size)
        }
    }
}