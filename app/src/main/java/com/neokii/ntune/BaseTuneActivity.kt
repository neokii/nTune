package com.neokii.ntune

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.neokii.ntune.ui.main.SectionsPagerAdapter
import org.json.JSONObject
import java.util.*

abstract class BaseTuneActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private var viewPager: ViewPager? = null
    private var sectionsPagerAdapter: SectionsPagerAdapter? = null

    private var tts: TextToSpeech? = null

    abstract fun getRemoteConfFile(): String
    abstract fun getItemList(json: JSONObject): ArrayList<TuneItemInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.getDefault()
            }
        }

        setContentView(R.layout.activity_tune)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        intent?.let {

            val session = SshSession(it.getStringExtra("host"), 8022)
            session.connect(object : SshSession.OnConnectListener {
                override fun onConnect() {
                    session.exec(
                        "cat ${getRemoteConfFile()}",
                        object : SshSession.OnResponseListener {
                            override fun onResponse(res: String) {
                                start(res)
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

    override fun onDestroy() {
        super.onDestroy()

        viewPager?.removeOnPageChangeListener(this)

        tts?.stop()
        tts?.shutdown()
    }

    private fun start(res: String)
    {
        intent?.let {

            try {

                val json = JSONObject(res)
                val list = getItemList(json)

                sectionsPagerAdapter = SectionsPagerAdapter(
                    this, supportFragmentManager, list,
                    it.getStringExtra("host"),
                    getRemoteConfFile()
                )
                val viewPager: ViewPager = findViewById(R.id.view_pager)
                viewPager.adapter = sectionsPagerAdapter
                val tabs: TabLayout = findViewById(R.id.tabs)
                tabs.setupWithViewPager(viewPager)

                viewPager.addOnPageChangeListener(this)
            }
            catch (e: Exception)
            {
                if(res.isEmpty())
                {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.conf_load_failed,
                        Snackbar.LENGTH_LONG
                    )
                        .show()
                }
                else
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
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {

        sectionsPagerAdapter?.let {

            try {
                val text = it.getPageTitle(position).toString()
                    .split(Regex("(?=[A-Z])"))
                    .joinToString(" ")

                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
            catch (e:Exception){}
        }
    }
}
