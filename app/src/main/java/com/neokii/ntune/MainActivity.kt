package com.neokii.ntune

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var session: SshSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_main)

        val hosts = SettingUtil.getStringList(applicationContext, "hosts") as List<String>
        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item, hosts)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerHost.setAdapter(adapter)

        if(hosts.isNotEmpty())
            spinnerHost.setText(hosts[0])

        btnConnectLqr.setOnClickListener {
            handleConnect(true)
        }

        btnConnectIndi.setOnClickListener {
            handleConnect(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        session?.close()
    }

    fun handleConnect(isLqr: Boolean)
    {
        val host = spinnerHost.text.toString();
        if(!host.isEmpty())
        {
            btnConnectLqr.isEnabled = false
            btnConnectIndi.isEnabled = false

            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(spinnerHost.getWindowToken(), 0)

            session = SshSession(host, 8022)
            session?.connect(object: SshSession.OnConnectListener
            {
                override fun onConnect() {

                    btnConnectLqr.isEnabled = true
                    btnConnectIndi.isEnabled = true

                    val hosts = SettingUtil.getStringList(applicationContext, "hosts")
                    hosts.remove(host)
                    hosts.add(0, host)
                    SettingUtil.setStringList(applicationContext, "hosts", hosts)

                    val intent = Intent(this@MainActivity,
                        if(isLqr) LqrTuneActivity::class.java else IndiTuneActivity::class.java)

                    intent.putExtra("host", host)
                    startActivity(intent)
                }

                override fun onFail(e: Exception) {
                    btnConnectLqr.isEnabled = true
                    btnConnectIndi.isEnabled = true
                    Snackbar.make(findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}
