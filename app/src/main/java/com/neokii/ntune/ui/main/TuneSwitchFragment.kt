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
import com.neokii.ntune.ui.main.TuneFragment.Companion.lastJson
import kotlinx.android.synthetic.main.fragment_switch_tune.*
import org.json.JSONObject

class TuneSwitchFragment : Fragment() {

    private lateinit var itemInfo: TuneItemInfo
    private lateinit var tuneViewModel: TuneViewModel

    private lateinit var remoteConfFile: String
    private lateinit var host: String
    private var session: SshSession? = null

    companion object {
        @JvmStatic
        fun newInstance(itemInfo: TuneItemInfo, host:String, remoteConfFile:String): TuneSwitchFragment {

            return TuneSwitchFragment().apply {
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

        val root = inflater.inflate(R.layout.fragment_switch_tune, container, false)
        tuneViewModel.value.observe(viewLifecycleOwner, Observer<Float> {
            //textValue.text = itemInfo.toString(it)
            switchEnable.isChecked = it > 0.5f
        })

        Handler().post {
            initControls()
        }

        return root
    }

    private fun initControls()
    {
        textKey.text = itemInfo.key

        if(itemInfo.descResId > 0)
            textDesc.setText(itemInfo.descResId)

        switchEnable.setOnClickListener {
            update( if(switchEnable.isChecked)  1.0f else 0.0f )
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
                enableButtons(true)
                updateValue()
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
        session?.exec("cat $remoteConfFile", object : SshSession.OnResponseListener{
            override fun onResponse(res: String) {
                try {
                    lastJson = JSONObject(res)

                    tuneViewModel.value.apply {
                        value = lastJson.getDouble(itemInfo.key).toFloat()
                    }
                }
                catch (e:Exception){}
            }

            override fun onEnd(e: Exception?) {
                enableButtons(true)
            }

        })
    }

    private fun update(value:Float)
    {
        try {
            lastJson.put(itemInfo.key, value)

            enableButtons(false)
            session?.exec("echo '${lastJson.toString(2)}' > $remoteConfFile", object : SshSession.OnResponseListener{
                override fun onResponse(res: String) {
                    updateValue()
                }

                override fun onEnd(e: Exception?) {
                    enableButtons(true)
                    if (e != null)
                        showSnackbar(e.localizedMessage)
                }
            })
        }
        catch (e:Exception){
            showSnackbar(e.localizedMessage)
        }
    }

    private fun enableButtons(enabled:Boolean)
    {
        try {
            switchEnable.isEnabled = enabled
        }
        catch (e:Exception){}
    }

    private fun showSnackbar(msg: String)
    {
        try {
            activity?.let {
                Snackbar.make(it.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        catch (e: Exception){}
    }
}