package com.github.rooneyandshadows.lightbulb.apt.android.core.parcelable

import android.os.Parcel
import android.os.Parcelable

open class BlankParcelable() : Parcelable {


    constructor(parcel: Parcel) : this() {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {

    }

    companion object CREATOR : Parcelable.Creator<BlankParcelable> {
        override fun createFromParcel(parcel: Parcel): BlankParcelable? {
            return null
        }

        override fun newArray(size: Int): Array<BlankParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
