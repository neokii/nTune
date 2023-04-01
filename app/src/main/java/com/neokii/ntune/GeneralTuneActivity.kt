package com.neokii.ntune

import org.json.JSONObject

class GeneralTuneActivity: BaseTuneActivity() {

    override fun getTuneKey(): String {
        return "General"
    }

    override fun getRemoteConfFile(): String {
        return "/data/ntune/common.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        val list = ArrayList<TuneItemInfo>()

        list.add(TuneItemInfo("pathOffset", json.getDouble("pathOffset").toFloat(),
            -1.0f, 1.0f, 0.01f, 2, R.string.tune_path_offset_desc))

        list.add(TuneItemInfo("pathFactor", json.getDouble("pathFactor").toFloat(),
            0.8f, 1.1f, 0.01f, 2, R.string.tune_path_factor_desc))

        if(!Feature.FEATURE_UNIVERSAL)
        {
            list.add(TuneItemInfo("useLiveSteerRatio", json.getDouble("useLiveSteerRatio").toFloat(),
                0f, 1.0f, 0f, 0, R.string.general_use_live_sr))
        }

        list.add(TuneItemInfo("steerRatio", json.getDouble("steerRatio").toFloat(),
            5.0f, 25.0f, 0.1f, 2))

        /*if(!Feature.FEATURE_UNIVERSAL)
        {
            list.add(TuneItemInfo("steerRateCost", json.getDouble("steerRateCost").toFloat(),
                0.1f, 1.5f, 0.05f, 3))
        }*/

        list.add(TuneItemInfo("steerActuatorDelay", json.getDouble("steerActuatorDelay").toFloat(),
            0.0f, 0.8f, 0.05f, 3))

        return list
    }
}