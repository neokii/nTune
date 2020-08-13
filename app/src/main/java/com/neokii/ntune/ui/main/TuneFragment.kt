package com.neokii.ntune.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.neokii.ntune.R
import com.neokii.ntune.SettingUtil
import com.neokii.ntune.SshSession
import com.neokii.ntune.TuneItemInfo
import kotlinx.android.synthetic.main.fragment_tune.*
import org.json.JSONObject
import kotlin.math.round


fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (round(this * multiplier) / multiplier).toFloat()
}

class TuneFragment : Fragment() {

    private lateinit var itemInfo: TuneItemInfo
    private lateinit var tuneViewModel: TuneViewModel

    private lateinit var remoteConfFile: String
    private lateinit var host: String
    private var session: SshSession? = null

    companion object {
        var lastJson = JSONObject()

        @JvmStatic
        fun newInstance(itemInfo: TuneItemInfo, host:String, remoteConfFile:String): TuneFragment {

            return TuneFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", itemInfo)
                    putString("host", host)
                    putString("remoteConfFile", remoteConfFile)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

            remoteConfFile = it.getString("remoteConfFile", "")
            host = it.getString("host", "")
            itemInfo = it.getParcelable("item")!!
            tuneViewModel = ViewModelProviders.of(this).get(TuneViewModel::class.java).apply {
                value.value = 0.0f
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_tune, container, false)
        tuneViewModel.value.observe(viewLifecycleOwner, Observer<Float> {
            textValue.text = itemInfo.toString(it)
        })

        Handler().post {
            initControls()
        }

        return root
    }

    private fun initControls()
    {
        textKey.text = itemInfo.key
        textMin.text = itemInfo.toString(itemInfo.min)
        textMax.text = itemInfo.toString(itemInfo.max)

        updateStepScale()

        btnIncrease.setOnClickListener {
            increase(1.0f * getStep())
        }

        btnDecrease.setOnClickListener {
            increase(-1.0f * getStep())
        }

        btnIncrease.setOnLongClickListener {
            increase(10.0f * getStep())
            return@setOnLongClickListener true
        }

        btnDecrease.setOnLongClickListener {
            increase(-10.0f * getStep())
            return@setOnLongClickListener true
        }

        btnReset.setOnClickListener {
            update(itemInfo.defValue)
        }

        activity?.let {
            val items = arrayOf("x0.1", "x0.5", "x1", "x5", "x10")

            spinnerStepScale.adapter =
                ArrayAdapter(it.applicationContext, R.layout.spinner_text,
                    items).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

            spinnerStepScale.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    SettingUtil.setInt(activity, "item_step_scale_" + itemInfo.key, position)
                    updateStepScale()
                }
            }

            spinnerStepScale.setSelection(SettingUtil.getInt(activity, "item_step_scale_" + itemInfo.key, 2))
        }


    }

    private fun getStep(): Float
    {
        try {
            val index = SettingUtil.getInt(activity, "item_step_scale_" + itemInfo.key, 2)
            val v = arrayOf(0.1f, 0.5f, 1.0f, 5.0f, 10.0f)
            return itemInfo.step*v[index]
        }
        catch (e: Exception){}
        return itemInfo.step
    }

    private fun updateStepScale()
    {
        textStep.text = "Step: ${getStep()}"
    }

    override fun onResume() {
        super.onResume()
        connect()
    }

    override fun onPause() {
        super.onPause()
        disconnect()
    }

    private fun connect()
    {
        enableButtons(false)
        session = SshSession(host, 8022)
        session?.connect(object: SshSession.OnConnectListener{
            override fun onConnect() {
                updateValue()
                enableButtons(true)
            }

            override fun onFail(e: Exception) {

                enableButtons(true)
                showSnackbar(e.localizedMessage)

            }
        })
    }

    private fun disconnect()
    {
        session?.close()
    }

    private fun updateValue()
    {
        enableButtons(false)
        session?.send("cat $remoteConfFile", object : SshSession.OnResponseListener{
            override fun onResponse(res: String) {
                try {
                    lastJson = JSONObject(res)

                    tuneViewModel.value.apply {
                        value = lastJson.getDouble(itemInfo.key).toFloat()
                    }
                }
                catch (e:Exception){}

                enableButtons(true)
            }

            override fun onFail(e: Exception) {
                enableButtons(true)
            }

        })
    }


    private fun increase(step:Float)
    {
        try {
            var value = lastJson.getDouble(itemInfo.key).toFloat()
            value += step
            value = value.round(itemInfo.precision)

            if(value < itemInfo.min)
                value = itemInfo.min

            if(value > itemInfo.max)
                value = itemInfo.max

            update(value)
        }
        catch (e:Exception){
            showSnackbar(e.localizedMessage)
        }
    }

    private fun update(value:Float)
    {
        try {
            lastJson.put(itemInfo.key, value)

            enableButtons(false)
            session?.send("echo '${lastJson.toString(2)}' > $remoteConfFile", object : SshSession.OnResponseListener{
                override fun onResponse(res: String) {
                    updateValue()
                    enableButtons(true)
                    sendInvalidate()
                }

                override fun onFail(e: Exception) {
                    enableButtons(true)
                    showSnackbar(e.localizedMessage)
                }
            })
        }
        catch (e:Exception){
            showSnackbar(e.localizedMessage)
        }
    }

    private fun sendInvalidate()
    {
        //session?.send("mkfifo /tmp/ntune_pipe || echo \"reload\" > /tmp/ntune_pipe", null)
    }

    private fun enableButtons(enabled:Boolean)
    {
        try {
            btnReset.isEnabled = enabled
            btnIncrease.isEnabled = enabled
            btnDecrease.isEnabled = enabled
        }
        catch (e:Exception){}
    }

    private fun showSnackbar(msg: String)
    {
        try {
            activity?.let {
                Snackbar.make(it.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
        catch (e: Exception){}
    }
}