package com.neokii.ntune

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.neokii.ntune.databinding.CmdButtonBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity(), SshShell.OnSshListener
{
    var session: SshSession? = null
    var shell: SshShell? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            //actionBar.setDisplayHomeAsUpEnabled(true)
            try {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                actionBar.title = getString(R.string.app_name) + " " + packageInfo.versionName

                val universal = if (Feature.FEATURE_UNIVERSAL) "Universal" else "HKG Only"

                actionBar.title = "${getString(R.string.app_name)} ${packageInfo.versionName} $universal"
            } catch (e: java.lang.Exception) {
            }
        }

        setContentView(R.layout.activity_main)

        SettingUtil.getString(applicationContext, "last_host", "").also {
            if(it.isNotEmpty())
                editHost.setText(it)
        }

        btnConnectLqr.setOnClickListener {
            handleConnect(LqrTuneActivity::class.java)
        }

        /*btnConnectIndi.setOnClickListener {
            handleConnect(IndiTuneActivity::class.java)
        }*/

        btnGeneral.setOnClickListener {
            handleConnect(GeneralTuneActivity::class.java)
        }

        btnExceptionCapture.setOnClickListener {

            val host = editHost.text.toString();
            if(host.isNotEmpty())
            {
                val intent = Intent(this, ExceptionCaptureActivity::class.java)
                intent.putExtra("host", host)
                startActivity(intent)
            }
        }

        btnScan.setOnClickListener {
            handleScan()
        }

        btnGitAccount.setOnClickListener {
            handleGitAccount()
        }

        btnGitAccount.visibility = View.GONE

        buildButtons()
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
            //btnConnectIndi.isEnabled = false

            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editHost.windowToken, 0)

            session = SshSession(host, 8022)
            session?.connect(object : SshSession.OnConnectListener {
                override fun onConnect() {

                    btnConnectLqr.isEnabled = true
                    //btnConnectIndi.isEnabled = true

                    SettingUtil.setString(applicationContext, "last_host", host)

                    val intent = Intent(this@MainActivity, cls)

                    intent.putExtra("host", host)
                    startActivity(intent)
                }

                override fun onFail(e: Exception) {

                    btnConnectLqr.isEnabled = true
                    //btnConnectIndi.isEnabled = true
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

    private fun handleGitAccount()
    {
        val f = GitAccountDialog()
        f.show(supportFragmentManager, null)
    }

    fun DP2PX(context: Context, dp: Int): Int {
        val resources: Resources = context.resources
        val metrics: DisplayMetrics = resources.getDisplayMetrics()
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun addLog(text: String)
    {
        if(logView.text.length > 1024*1024)
            logView.text = ""

        logView.append(text + "\n")
        logScrollView.post {
            logScrollView.smoothScrollTo(0, logView.bottom)
        }
    }

    inner class ListAdapter: RecyclerView.Adapter<ListAdapter.ViewHolder>()
    {
        inner class ViewHolder(val binding: CmdButtonBinding) : RecyclerView.ViewHolder(binding.root)
        {
            init {
                binding.btnCmd.setOnClickListener {
                    val cmd = SshShell.cmdList[adapterPosition]

                    val host = editHost.text.toString();
                    if(host.isNotEmpty())
                    {
                        if(cmd.confirm)
                        {
                            AlertDialog.Builder(this@MainActivity)
                                .setMessage(R.string.confirm)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(
                                    android.R.string.ok
                                ) { _, _ ->
                                    shell(host, cmd.cmds)
                                }
                                .show()
                        }
                        else
                        {
                            shell(host, cmd.cmds)
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return SshShell.cmdList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(CmdButtonBinding.inflate(layoutInflater))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val cmd = SshShell.cmdList[position]
            holder.binding.btnCmd.text = getString(cmd.resId)
        }
    }

    private fun buildButtons()
    {
        listView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        listView.adapter = ListAdapter()
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

    override fun onRead(res: String) {
        addLog(res)
    }


}
