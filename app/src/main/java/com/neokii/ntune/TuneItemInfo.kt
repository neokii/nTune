package com.neokii.ntune

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.lang.Exception

data class TuneItemInfo(val key: String, val defValue: Float, val min: Float,
                        val max: Float, val step: Float, val precision: Int) : Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeFloat(defValue)
        parcel.writeFloat(min)
        parcel.writeFloat(max)
        parcel.writeFloat(step)
        parcel.writeInt(precision)
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

        fun fromJson(jsonObject: JSONObject) : TuneItemInfo?
        {
            try
            {
                return TuneItemInfo(jsonObject.getString("key"),
                    jsonObject.getDouble("defValue").toFloat(),
                    jsonObject.getDouble("min").toFloat(),
                    jsonObject.getDouble("max").toFloat(),
                    jsonObject.getDouble("step").toFloat(),
                    jsonObject.getDouble("precision").toInt()
                )
            }
            catch (e: Exception){}
            return null
        }
    }

    fun toJsonString() : String?
    {
        try
        {
            val json = JSONObject()

            json.put("key", key)
            json.put("defValue", defValue)
            json.put("min", min)
            json.put("max", max)
            json.put("step", step)
            json.put("precision", precision)

            return json.toString()
        }
        catch (e: Exception){}
        return null
    }

    fun toString(value: Float) : String
    {
        return "%.${precision}f".format(value)
    }
}