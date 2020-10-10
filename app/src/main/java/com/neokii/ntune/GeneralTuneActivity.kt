package com.neokii.ntune

import org.json.JSONObject

class GeneralTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/common.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        val list = ArrayList<TuneItemInfo>()

        list.add(TuneItemInfo("steerRatio", json.getDouble("steerRatio").toFloat(),
            5.0f, 25.0f, 0.1f, 2))

        list.add(TuneItemInfo("steerActuatorDelay", json.getDouble("steerActuatorDelay").toFloat(),
            0.1f, 0.8f, 0.05f, 3))

        return list
    }
}