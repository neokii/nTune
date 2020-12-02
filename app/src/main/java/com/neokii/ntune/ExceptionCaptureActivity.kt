package com.neokii.ntune

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.exception_capture.*
import javax.xml.parsers.FactoryConfigurationError

class ExceptionCaptureActivity : AppCompatActivity(), SshShell.OnSshListener {

    lateinit var editLog: EditText
    private var host: String? = null
    private var shell: SshShell? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.exception_capture)
        editLog = findViewById(R.id.editLog)

        intent?.let {
            host = it.getStringExtra("host")
            host?.let { h ->
                shell(h, arrayListOf("tmux a"))
            }
        }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("Exception", editLog.text)
            clipboard.setPrimaryClip(clip)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shell?.close()
    }

    fun shell(host: String, cmds: ArrayList<String>)
    {
        if(shell == null || shell?.host != host || !shell?.isConnected()!!) {

            if(shell != null)
                shell?.close()

            shell = SshShell(host, 8022, this)
            shell?.start()
        }

        if(shell?.isConnected() == true)
            addLog("\n")

        for (cmd in cmds)
            shell?.send(cmd)
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
            matched = res.startsWith("Traceback (")

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
}