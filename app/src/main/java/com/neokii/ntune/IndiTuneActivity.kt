package com.neokii.ntune

import com.neokii.ntune.Feature.FEATURE_UNIVERSAL
import org.json.JSONObject

class IndiTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_indi.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return  ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("innerLoopGain", json.getDouble("innerLoopGain").toFloat(), 0.5f, 10.0f, 0.05f, 3))
            add(TuneItemInfo("outerLoopGain", json.getDouble("outerLoopGain").toFloat(), 0.5f, 10.0f, 0.05f, 3))
            add(TuneItemInfo("timeConstant", json.getDouble("timeConstant").toFloat(), 0.1f, 5.0f, 0.05f, 3))
            add(TuneItemInfo("actuatorEffectiveness", json.getDouble("actuatorEffectiveness").toFloat(), 0.1f, 5.0f, 0.05f, 3))

            add(TuneItemInfo("steerLimitTimer", json.getDouble("steerLimitTimer").toFloat(),
             0.5f, 3.0f, 0.05f, 3))

            add(TuneItemInfo("steerMax", json.getDouble("steerMax").toFloat(),
                0.5f, 3.0f, 0.05f, 3))
        }
    }
}