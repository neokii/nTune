package com.neokii.ntune

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.setMargins
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SshShell.OnSshListener
{

    var session: SshSession? = null
    var shell: SshShell? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_main)

        val lastIp = SettingUtil.getString(applicationContext, "last_host", "")
        if(lastIp.isNotEmpty())
            editHost.setText(lastIp)

        btnConnectLqr.setOnClickListener {
            handleConnect(LqrTuneActivity::class.java)
        }

        btnConnectIndi.setOnClickListener {
            handleConnect(IndiTuneActivity::class.java)
        }

        btnCommon.setOnClickListener {
            handleConnect(CommonTuneActivity::class.java)
        }

        btnScan.setOnClickListener {
            handleScan()
        }

        buildButtons(layoutButtons)
        updateControls(false)

        handleScan()
    }

    override fun onDestroy() {
        super.onDestroy()

        session?.close()
        shell?.close()
    }

    private fun handleConnect(cls: Class<out BaseTuneActivity>)
    {
        val host = editHost.text.toString();
        if(host.isNotEmpty())
        {
            btnConnectLqr.isEnabled = false
            btnConnectIndi.isEnabled = false

            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editHost.windowToken, 0)

            session = SshSession(host, 8022)
            session?.connect(object : SshSession.OnConnectListener {
                override fun onConnect() {

                    btnConnectLqr.isEnabled = true
                    btnConnectIndi.isEnabled = true

                    SettingUtil.setString(applicationContext, "last_host", host)

                    val intent = Intent(this@MainActivity, cls)

                    intent.putExtra("host", host)
                    startActivity(intent)
                }

                override fun onFail(e: Exception) {

                    btnConnectLqr.isEnabled = true
                    btnConnectIndi.isEnabled = true
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

    private fun updateControls(pending: Boolean)
    {
        if(pending)
        {
            progBar.visibility = View.VISIBLE
            btnScan.isEnabled = false
            editHost.isEnabled = false
        }
        else
        {
            progBar.visibility = View.INVISIBLE
            btnScan.isEnabled = true
            editHost.isEnabled = true
        }
    }

    var pendingScan: Boolean = false

    private fun handleScan()
    {
        if(pendingScan)
            return

        pendingScan = true
        updateControls(pendingScan)
        EonScanner().startScan(applicationContext, 8022, object : EonScanner.OnResultListener {
            override fun onResult(ip: String?) {

                pendingScan = false
                updateControls(pendingScan)

                if (!TextUtils.isEmpty(ip)) {
                    editHost.setText(ip)
                    SettingUtil.setString(applicationContext, "last_host", ip)

                    Snackbar.make(
                        findViewById(android.R.id.content), getString(
                            R.string.format_scan_success,
                            ip
                        ), Snackbar.LENGTH_LONG
                    )
                        .show()
                } else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.scan_fail),
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
            }
        })
    }

    fun DP2PX(context: Context, dp: Int): Int {
        val resources: Resources = context.resources
        val metrics: DisplayMetrics = resources.getDisplayMetrics()
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private var lastLog: String? = null
    private fun addLog(text: String)
    {
        if(lastLog != null && lastLog.equals(text) && !lastLog!!.matches(Regex(""".+@.+:.+\$\s.+""")))
            return

        lastLog = text

        if(logView.text.length > 1024*1024)
            logView.text = ""

        logView.append(text + "\n")
        logScrollView.post {
            logScrollView.smoothScrollTo(0, logView.bottom)
        }
    }

    private fun buildButtons(viewGroup: ViewGroup)
    {
        for(cmd in SshShell.cmdList)
        {
            val btn = Button(this)
            btn.text = cmd.key

            GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            ).also {

                it.setMargins(DP2PX(this, 8))
                viewGroup.addView(btn, it)
            }

            btn.setOnClickListener{

                val host = editHost.text.toString();
                if(host.isNotEmpty())
                {
                    if(cmd.value.confirm)
                    {
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage(R.string.confirm)
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(
                                android.R.string.ok
                            ) { _, _ ->
                                SshShell.cmdList[cmd.key]?.let { shell(host, it.cmds) }
                            }
                            .show()
                    }
                    else
                    {
                        SshShell.cmdList[cmd.key]?.let { shell(host, it.cmds) }
                    }
                }
            }
        }
    }

    fun shell(host: String, cmds: ArrayList<String>)
    {
        if(shell == null || shell?.host != host || !shell?.isConnected()!!) {

            if(shell != null)
                shell?.close()

            shell = SshShell(host, 8022, this)
            shell?.start()
        }

        for (cmd in cmds)
            shell?.send(cmd)
    }

    override fun onConnect() {
        TODO("Not yet implemented")
    }

    override fun onError(e: java.lang.Exception) {
        TODO("Not yet implemented")
    }

    override fun onRead(res: String) {
        addLog(res)
    }


}
