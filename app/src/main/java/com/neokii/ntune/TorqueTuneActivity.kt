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

            //add(TuneItemInfo("liveTorqueParams", json.getDouble("liveTorqueParams").toFloat(),
            //    0f, 1.0f, 0f, 0, R.string.torque_live_params))
            add(TuneItemInfo("latAccelFactor", json.getDouble("latAccelFactor").toFloat(), 0.5f, 4.5f, 0.1f, 3))
            add(TuneItemInfo("friction", json.getDouble("friction").toFloat(), 0.0f, 0.2f, 0.01f, 3))
            add(TuneItemInfo("angle_deadzone_v2", json.getDouble("angle_deadzone_v2").toFloat(),
                0.0f, 2.0f, 0.01f, 2))

            //add(TuneItemInfo("useSteeringAngle", json.getDouble("useSteeringAngle").toFloat(),
            //    0f, 1.0f, 0f, 0))
        }
    }
}