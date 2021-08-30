package com.sinbaram.mapgo.Model

import android.os.Parcel
import android.os.Parcelable

data class NewFeedModel(
    var location: String? = "위치",
    var keywords: List<String>? = listOf(),
    var contents: String? = "본문"
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createStringArrayList()!!.toList(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(location)
        parcel.writeStringList(keywords)
        parcel.writeString(contents)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewFeedModel> {
        override fun createFromParcel(parcel: Parcel): NewFeedModel {
            return NewFeedModel(parcel)
        }

        override fun newArray(size: Int): Array<NewFeedModel?> {
            return arrayOfNulls(size)
        }
    }
}
