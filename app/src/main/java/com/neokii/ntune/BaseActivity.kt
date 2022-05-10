package com.neokii.ntune

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateOrientation()
    }

    protected fun updateOrientation() {
        val lock_portrait = SettingUtil.getBoolean(applicationContext, "lock_portrait", false)
        if(lock_portrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
    }

}