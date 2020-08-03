package com.neokii.ntune.ui.main

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.neokii.ntune.R
import com.neokii.ntune.SshSession
import com.neokii.ntune.TuneItemInfo
import kotlinx.android.synthetic.main.fragment_tune.*
import org.json.JSONObject
import java.lang.Exception
import kotlin.math.round
import kotlin.math.roundToInt

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (round(this * multiplier) / multiplier).toFloat()
}

class TuneFragment : Fragment() {

    private lateinit var itemInfo: TuneItemInfo
    private lateinit var tuneViewModel: TuneViewModel

    private lateinit var host: String
    private var session: SshSession? = null

    companion object {

        val CONF_FILE = "/data/ntune/lat_lqr.json"
        var lastJson = JSONObject()

        @JvmStatic
        fun newInstance(itemInfo: TuneItemInfo, host:String): TuneFragment {

            return TuneFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("item", itemInfo)
                    putString("host", host)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

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
        textStep.text = "Step: ${itemInfo.step}"

        btnIncrease.setOnClickListener {
            increase(1.0f * itemInfo.step)
        }

        btnDecrease.setOnClickListener {
            increase(-1.0f * itemInfo.step)
        }

        btnIncrease.setOnLongClickListener {
            increase(10.0f * itemInfo.step)
            return@setOnLongClickListener true
        }

        btnDecrease.setOnLongClickListener {
            increase(-10.0f * itemInfo.step)
            return@setOnLongClickListener true
        }

        btnReset.setOnClickListener {
            update(itemInfo.defValue)
        }
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
        session?.send("cat $CONF_FILE", object : SshSession.OnResponseListener{
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
            session?.send("echo '${lastJson.toString(2)}' > $CONF_FILE", object : SshSession.OnResponseListener{
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