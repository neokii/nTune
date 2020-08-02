package com.neokii.ntune

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    var session: SshSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val hosts = SettingUtil.getStringList(applicationContext, "hosts") as List<String>
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, hosts)

        spinnerHost.setAdapter(adapter)

        if(hosts.isNotEmpty())
            spinnerHost.setText(hosts[0])

        btnConnect.setOnClickListener {
            handleConnect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        session?.close()
    }

    fun handleConnect()
    {
        val host = spinnerHost.text.toString();
        if(!host.isEmpty())
        {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(spinnerHost.getWindowToken(), 0)

            session = SshSession(host, 8022)
            session?.connect(object: SshSession.OnConnectListener
            {
                override fun onConnect() {

                    val hosts = SettingUtil.getStringList(applicationContext, "hosts")
                    hosts.remove(host)
                    hosts.add(0, host)
                    SettingUtil.setStringList(applicationContext, "hosts", hosts)

                    val intent = Intent(this@MainActivity, TuneActivity::class.java)
                    intent.putExtra("host", host)
                    startActivity(intent)
                }

                override fun onFail(e: Exception) {
                    Snackbar.make(findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}
