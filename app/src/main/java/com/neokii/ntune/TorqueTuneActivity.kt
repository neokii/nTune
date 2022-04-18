package com.neokii.ntune

import org.json.JSONObject

class TorqueTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_torque.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("useSteeringAngle", json.getDouble("useSteeringAngle").toFloat(),
                0f, 1.0f, 0f, 0))
            add(TuneItemInfo("kp", json.getDouble("kp").toFloat(), 0.5f, 3.0f, 0.1f, 3))
            add(TuneItemInfo("kf", json.getDouble("kf").toFloat(), 0.0f, 0.5f, 0.01f, 3))
            add(TuneItemInfo("friction", json.getDouble("friction").toFloat(), 0.0f, 1.0f, 0.01f, 3))
            add(TuneItemInfo("ki", json.getDouble("ki").toFloat(), 0.0f, 0.5f, 0.01f, 3))
            add(TuneItemInfo("kd", json.getDouble("kd").toFloat(), 0.0f, 1.5f, 0.1f, 2))
        }
    }
}