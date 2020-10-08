package com.neokii.ntune

import org.json.JSONObject

class LqrTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_lqr.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return ArrayList<TuneItemInfo>().also {

            it.add(TuneItemInfo("scale", json.getDouble("scale").toFloat(), 500.0f, 5000.0f, 50.0f, 1))
            it.add(TuneItemInfo("ki", json.getDouble("ki").toFloat(), 0.0f, 0.2f, 0.01f, 3))

            //it.add(TuneItemInfo("k_1", json.getDouble("k_1").toFloat(), -150.0f, -50.0f, 5.0f, 1))
            //it.add(TuneItemInfo("k_2", json.getDouble("k_2").toFloat(), 400.0f, 500.0f, 5.0f, 1))

            //it.add(TuneItemInfo("l_1", json.getDouble("l_1").toFloat(), 0.1f, 0.5f, 0.01f, 3))
            //it.add(TuneItemInfo("l_2", json.getDouble("l_2").toFloat(), 0.1f, 0.5f, 0.01f, 3))

            it.add(TuneItemInfo("dcGain", json.getDouble("dcGain").toFloat(), 0.0020f, 0.0040f, 0.0001f, 5))

            it.add(TuneItemInfo("steerLimitTimer", json.getDouble("steerLimitTimer").toFloat(),
                0.5f, 3.0f, 0.05f, 3))

            it.add(TuneItemInfo("steerMax", json.getDouble("steerMax").toFloat(),
                0.5f, 3.0f, 0.05f, 3))
        }
    }
}