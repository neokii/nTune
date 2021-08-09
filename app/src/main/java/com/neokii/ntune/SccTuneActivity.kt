package com.neokii.ntune

import org.json.JSONObject

class SccTuneActivity: BaseTuneActivity() {

    override fun getRemoteConfFile(): String {
        return "/data/ntune/scc.json"
    }

    override fun getItemList(json: JSONObject): ArrayList<TuneItemInfo> {

        return  ArrayList<TuneItemInfo>().apply {

            add(TuneItemInfo("sccGasFactor", json.getDouble("sccGasFactor").toFloat(), 0.5f, 1.5f, 0.05f, 2))
            add(TuneItemInfo("sccBrakeFactor", json.getDouble("sccBrakeFactor").toFloat(), 0.5f, 1.5f, 0.05f, 2))
            add(TuneItemInfo("sccCurvatureFactor", json.getDouble("sccCurvatureFactor").toFloat(), 0.5f, 1.5f, 0.05f, 2))
        }
    }
}