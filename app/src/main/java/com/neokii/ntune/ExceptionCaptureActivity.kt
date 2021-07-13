package com.neokii.ntune

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.exception_capture.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ExceptionCaptureActivity : AppCompatActivity(), SshShell.OnSshListener {

    lateinit var editLog: EditText
    private var host: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.exception_capture)
        editLog = findViewById(R.id.editLog)

        intent?.let {
            host = it.getStringExtra("host")
            host?.let { h ->

                val session = SshSession(it.getStringExtra("host"), 8022)
                session.connect(object : SshSession.OnConnectListener {
                    override fun onConnect() {
                        session.exec(
                            "ls -tr /data/log | tail -1",
                            object : SshSession.OnResponseListener {
                                override fun onResponse(res: String) {

                                    if(res.isNullOrEmpty()) {
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            R.string.eon_no_log,
                                            Snackbar.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    else {
                                        getLog(session, res)
                                    }
                                }

                                override fun onEnd(e: Exception?) {

                                    if (e != null) {
                                        Snackbar.make(
                                            findViewById(android.R.id.content),
                                            e.localizedMessage,
                                            Snackbar.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                }
                            })
                    }

                    override fun onFail(e: Exception) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            e.localizedMessage,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }

                })
            }
        }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Exception", editLog.text)
            clipboard.setPrimaryClip(clip)
        }
    }

    fun getLog(session: SshSession, file: String) {

        session.exec(
            "cat /data/log/$file",
            object : SshSession.OnResponseListener {
                override fun onResponse(res: String) {

                    parseJson(res)
                }

                override fun onEnd(e: Exception?) {

                    if (e != null) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            e.localizedMessage,
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
            })
    }


    override fun onConnect() {
        TODO("Not yet implemented")
    }

    override fun onError(e: java.lang.Exception) {
        TODO("Not yet implemented")
    }

    var matched = false
    var connected = false
    override fun onRead(res: String) {

        if(!connected)
        {
            connected = true
            addLog(getString(R.string.connected_exception_capture))
        }

        if(matched)
        {
            addLog(res)
            if(res.matches(Regex("[A-Za-z]+(Error|Exception|Iteration): .+")))
                matched = false
        }
        else
        {
            matched = res.contains("Traceback (")

            if(matched)
                addLog(res)
        }
    }

    private fun addLog(msg: String)
    {
        if(editLog.text.length > 1024*1024*2)
            editLog.text.clear()

        editLog.append(msg)
        editLog.append("\n")
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseJson(jsonText: String)
    {
        try {
            val json = JSONObject(jsonText)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: String = simpleDateFormat.format(Date((json.getDouble("created")*1000L).toLong()))
            addLog("[$date]\n\n${json.getString("exc_info")}")
        }
        catch (e: Exception)
        {
            Snackbar.make(
                findViewById(android.R.id.content),
                e.localizedMessage,
                Snackbar.LENGTH_LONG
            )
                .show()
        }
    }
}