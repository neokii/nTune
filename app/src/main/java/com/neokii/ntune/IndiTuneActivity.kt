package com.neokii.ntune

import org.json.JSONObject

class IndiTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_indi.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        val list = ArrayList<TuneItemInfo>()

        list.add(TuneItemInfo("innerLoopGain", json.getDouble("innerLoopGain").toFloat(), 0.5f, 10.0f, 0.05f, 3))
        list.add(TuneItemInfo("outerLoopGain", json.getDouble("outerLoopGain").toFloat(), 0.5f, 10.0f, 0.05f, 3))
        list.add(TuneItemInfo("timeConstant", json.getDouble("timeConstant").toFloat(), 0.1f, 5.0f, 0.05f, 3))
        list.add(TuneItemInfo("actuatorEffectiveness", json.getDouble("actuatorEffectiveness").toFloat(), 0.1f, 5.0f, 0.05f, 3))

        return list
    }
}