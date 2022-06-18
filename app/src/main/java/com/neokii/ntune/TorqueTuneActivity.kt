package com.neokii.ntune

import org.json.JSONObject

class TorqueTuneActivity: BaseTuneActivity() {

    override fun getTuneKey(): String {
        return "Torque"
    }

    override fun getRemoteConfFile(): String {
        return "/data/ntune/lat_torque_v4.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("maxLatAccel", json.getDouble("maxLatAccel").toFloat(), 0.5f, 4.0f, 0.1f, 3))
            add(TuneItemInfo("friction", json.getDouble("friction").toFloat(), 0.0f, 0.2f, 0.01f, 3))
            add(TuneItemInfo("ki_factor", json.getDouble("ki_factor").toFloat(), 0.0f, 1.0f, 0.1f, 2))
            add(TuneItemInfo("kd", json.getDouble("kd").toFloat(), 0.0f, 2.0f, 0.1f, 2))
            add(TuneItemInfo("angle_deadzone_v2", json.getDouble("angle_deadzone_v2").toFloat(),
                0.0f, 2.0f, 0.01f, 2))

            add(TuneItemInfo("useSteeringAngle", json.getDouble("useSteeringAngle").toFloat(),
                0f, 1.0f, 0f, 0))
        }
    }
}