package com.neokii.ntune

import android.os.Parcel
import android.os.Parcelable

data class TuneItemInfo(val key: String, val defValue: Float, val min: Float,
                        val max: Float, val step: Float, val precision: Int, val descResId: Int = 0) : Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeFloat(defValue)
        parcel.writeFloat(min)
        parcel.writeFloat(max)
        parcel.writeFloat(step)
        parcel.writeInt(precision)
        parcel.writeInt(descResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TuneItemInfo> {

        override fun createFromParcel(parcel: Parcel): TuneItemInfo {
            return TuneItemInfo(parcel)
        }

        override fun newArray(size: Int): Array<TuneItemInfo?> {
            return arrayOfNulls(size)
        }
    }

    fun toString(value: Float) : String
    {
        return "%.${precision}f".format(value)
    }
}