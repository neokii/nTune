package com.neokii.ntune

import org.json.JSONObject

class IndiTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_indi.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("actuatorEffectiveness", json.getDouble("actuatorEffectiveness").toFloat(),
                0.5f, 3.0f, 0.1f, 2, R.string.lat_indi_actuatorEffectiveness))
            add(TuneItemInfo("timeConstant", json.getDouble("timeConstant").toFloat(), 0.5f, 3.0f, 0.1f, 2,
                R.string.lat_indi_timeConstant))
            add(TuneItemInfo("innerLoopGain", json.getDouble("innerLoopGain").toFloat(), 1.0f, 5.0f, 0.1f, 2,
                R.string.lat_indi_innerLoopGain))
            add(TuneItemInfo("outerLoopGain", json.getDouble("outerLoopGain").toFloat(), 1.0f, 5.0f, 0.1f, 2,
                R.string.lat_indi_outerLoopGain))
        }
    }
}