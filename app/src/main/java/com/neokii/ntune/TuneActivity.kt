package com.neokii.ntune

import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.neokii.ntune.ui.main.SectionsPagerAdapter
import com.neokii.ntune.ui.main.TuneFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class TuneActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    var viewPager: ViewPager? = null
    var sectionsPagerAdapter: SectionsPagerAdapter? = null

    var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, object : TextToSpeech.OnInitListener{
            override fun onInit(status: Int) {
                if (status != TextToSpeech.ERROR) {
                    tts?.setLanguage (Locale.getDefault())
                }
            }

        })

        setContentView(R.layout.activity_tune)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        intent?.let {

            val session = SshSession(it.getStringExtra("host"), 8022)
            session.connect(object : SshSession.OnConnectListener{
                override fun onConnect() {
                    session.send("cat ${TuneFragment.CONF_FILE}", object : SshSession.OnResponseListener{
                        override fun onResponse(res: String) {
                            start(res)
                        }

                        override fun onFail(e: Exception) {
                            Snackbar.make(findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    })
                }

                override fun onFail(e: Exception) {
                    Snackbar.make(findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT)
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

                val list = ArrayList<TuneItemInfo>()

                list.add(TuneItemInfo("scale", json.getDouble("scale").toFloat(), 500.0f, 5000.0f, 50.0f, 1))
                list.add(TuneItemInfo("ki", json.getDouble("ki").toFloat(), 0.0f, 0.2f, 0.01f, 2))

                list.add(TuneItemInfo("k_1", json.getDouble("k_1").toFloat(), -150.0f, -50.0f, 5.0f, 1))
                list.add(TuneItemInfo("k_2", json.getDouble("k_2").toFloat(), 400.0f, 500.0f, 5.0f, 1))

                list.add(TuneItemInfo("l_1", json.getDouble("l_1").toFloat(), 0.1f, 0.5f, 0.01f, 2))
                list.add(TuneItemInfo("l_2", json.getDouble("l_2").toFloat(), 0.1f, 0.5f, 0.01f, 2))

                list.add(TuneItemInfo("dcGain", json.getDouble("dcGain").toFloat(), 0.0020f, 0.0040f, 0.0001f, 4))

                list.add(TuneItemInfo("steerActuatorDelay", json.getDouble("steerActuatorDelay").toFloat(),
                    0.1f, 0.8f, 0.05f, 2))

                list.add(TuneItemInfo("steerLimitTimer", json.getDouble("steerLimitTimer").toFloat(),
                    0.6f, 1.5f, 0.05f, 2))

                list.add(TuneItemInfo("steerMax", json.getDouble("steerMax").toFloat(),
                    1.0f, 2.0f, 0.05f, 2))

                sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, list, it.getStringExtra("host"))
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
                    Snackbar.make(findViewById(android.R.id.content), R.string.conf_load_failed, Snackbar.LENGTH_SHORT)
                        .show()
                }
                else
                {
                    Snackbar.make(findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT)
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
            tts?.speak(it.getPageTitle(position).toString(), TextToSpeech.QUEUE_FLUSH , null )
        }
    }
}
