package com.neokii.ntune

import org.json.JSONObject

class SccTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/scc.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return  ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("longitudinalActuatorDelayLowerBound", json.getDouble("longitudinalActuatorDelayLowerBound").toFloat(), 0.1f, 1.5f, 0.01f, 3))
            add(TuneItemInfo("longitudinalActuatorDelayUpperBound", json.getDouble("longitudinalActuatorDelayUpperBound").toFloat(), 0.1f, 1.5f, 0.01f, 3))
            add(TuneItemInfo("sccGasFactor", json.getDouble("sccGasFactor").toFloat(), 0.5f, 1.5f, 0.01f, 3))
            add(TuneItemInfo("sccBrakeFactor", json.getDouble("sccBrakeFactor").toFloat(), 0.5f, 1.5f, 0.01f, 3))
            add(TuneItemInfo("sccCurvatureFactor", json.getDouble("sccCurvatureFactor").toFloat(), 0.5f, 1.5f, 0.01f, 3))
            //add(TuneItemInfo("vCruiseFactor", json.getDouble("vCruiseFactor").toFloat(), 0.8f, 1.2f, 0.01f, 3,
            //    R.string.tune_scc_v_cruise_factor))
        }
    }
}