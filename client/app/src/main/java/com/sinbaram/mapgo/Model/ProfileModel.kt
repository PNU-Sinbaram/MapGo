package com.sinbaram.mapgo.Model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

data class ProfileModel(
    var profile: Bitmap? = null,
    var nickname: String? = "user",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(profile, flags)
        parcel.writeString(nickname)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileModel> {
        override fun createFromParcel(parcel: Parcel): ProfileModel {
            return ProfileModel(parcel)
        }

        override fun newArray(size: Int): Array<ProfileModel?> {
            return arrayOfNulls(size)
        }
    }

}
