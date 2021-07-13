package com.neokii.ntune

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.exception_capture.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ExceptionCaptureActivity : AppCompatActivity() {

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

                                    getLog(session, res, true)
                                    getLog(session, res, false)
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

    fun getLog(session: SshSession, file: String, json:Boolean) {

        session.exec(
            "cat /data/log/$file",
            object : SshSession.OnResponseListener {
                override fun onResponse(res: String) {
                    if(json)
                        parseJson(res)
                    else
                        addLog(res)
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

    private fun addLog(msg: String)
    {
        if(editLog.text.length > 1024*1024*2)
            editLog.text.clear()

        editLog.append(msg)
        editLog.append("\n")
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseJson(text: String)
    {
        text.split("\n").forEach {

            try {
                val json = JSONObject(it)

                var count = 0
                if(json.has("level") && json.getString("level") == "ERROR")
                {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val date: String = simpleDateFormat.format(Date((json.getDouble("created")*1000L).toLong()))

                    if (json.has("exc_info")) {
                        addLog("[$date]\n\n${json.getString("exc_info")}")
                        count++
                    }
                    else if(json.has("msg\$s")) {
                        addLog("[$date]\n\n${json.getString("msg\$s")}")
                        count++
                    }
                }

                /*if(count == 0) {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.eon_no_log,
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }*/
            }
            catch (e: Exception)
            {
            }
        }
    }
}