package com.neokii.ntune.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class TuneViewModel : ViewModel() {

    val value = MutableLiveData<Float>()
}