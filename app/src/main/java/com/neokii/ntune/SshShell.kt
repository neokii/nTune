package com.neokii.ntune

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.*
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList

class SshShell(
    val host: String,
    private val port: Int,
    private val listener: OnSshListener
) : Thread()
{
    data class CmdItem(
        val resId: Int,
        val confirm: Boolean,
        val cmds: ArrayList<String>
    )

    companion object {
        val cmdList_eon = arrayListOf(

            CmdItem(R.string.shell_create_prebuilt, false, arrayListOf("touch /data/openpilot/prebuilt")),
            CmdItem(R.string.shell_remove_prebuilt, false, arrayListOf("rm /data/openpilot/prebuilt")),
            CmdItem(R.string.shell_git_reset_hard, true, arrayListOf("cd /data/openpilot && git reset --hard")),
            CmdItem(R.string.shell_git_reset_hard_back_1, true, arrayListOf("cd /data/openpilot && git reset --hard HEAD~1")),
            CmdItem(R.string.shell_git_pull, false, arrayListOf("cd /data/openpilot && git pull")),
            CmdItem(R.string.shell_git_change_branch, false, arrayListOf("git_change_branch")),
            CmdItem(R.string.shell_launch_android_settings, false, arrayListOf("am start -a android.settings.SETTINGS")),
            CmdItem(R.string.shell_kill_android_settings, false, arrayListOf("pkill -f com.android.settings")),
            CmdItem(R.string.shell_reset_calibration, true, arrayListOf("rm /data/params/d/CalibrationParams")),
            CmdItem(R.string.shell_reset_liveparams, true, arrayListOf("rm /data/params/d/LiveParameters")),
            CmdItem(R.string.shell_remove_realdata, true, arrayListOf("rm -rf /sdcard/realdata")),
            CmdItem(R.string.shell_remove_videos, true, arrayListOf("rm -rf /sdcard/videos")),
            CmdItem(R.string.shell_flash_panda, true, arrayListOf("cd /data/openpilot/panda/board", "tmux kill-server", "./flash.sh && reboot")),
            CmdItem(R.string.shell_recover_panda, true, arrayListOf("cd /data/openpilot/panda/board", "tmux kill-server", "./recover.sh && reboot")),
            CmdItem(R.string.shell_launch_navdy_settings, false, arrayListOf("am start -n com.neokii.openpilot/.MainActivity")),
            CmdItem(R.string.shell_clear_dtc, true, arrayListOf("cd /data/openpilot/selfdrive/debug", "pkill -f boardd", "./clear_dtc.py",
                "reboot")),
            CmdItem(R.string.shell_rebuild, true, arrayListOf("cd /data/openpilot", "scons -c", "rm .sconsign.dblite",
                "rm -rf /tmp/scons_cache", "rm prebuilt",
                "reboot")),
            CmdItem(R.string.shell_reboot, true, arrayListOf("reboot"))
        )

        val cmdList_tici = arrayListOf(

            CmdItem(R.string.shell_create_prebuilt, false, arrayListOf("touch /data/openpilot/prebuilt")),
            CmdItem(R.string.shell_remove_prebuilt, false, arrayListOf("rm /data/openpilot/prebuilt")),
            CmdItem(R.string.shell_git_reset_hard, true, arrayListOf("cd /data/openpilot && git reset --hard")),
            CmdItem(R.string.shell_git_reset_hard_back_1, true, arrayListOf("cd /data/openpilot && git reset --hard HEAD~1")),
            CmdItem(R.string.shell_git_pull, false, arrayListOf("cd /data/openpilot && git pull")),
            CmdItem(R.string.shell_git_change_branch, false, arrayListOf("git_change_branch")),
            CmdItem(R.string.shell_reset_calibration, true, arrayListOf("rm /data/params/d/CalibrationParams")),
            CmdItem(R.string.shell_reset_liveparams, true, arrayListOf("rm /data/params/d/LiveParameters")),
            CmdItem(R.string.shell_remove_realdata, true, arrayListOf("rm -rf /data/media/0/realdata")),
            CmdItem(R.string.shell_remove_videos, true, arrayListOf("rm -rf /data/media/0/videos")),
            CmdItem(R.string.shell_flash_panda, true, arrayListOf("cd /data/openpilot/panda/board", "tmux kill-server", "./flash.sh && sudo reboot")),
            CmdItem(R.string.shell_recover_panda, true, arrayListOf("cd /data/openpilot/panda/board", "tmux kill-server", "./recover.sh && sudo reboot")),
            CmdItem(R.string.shell_clear_dtc, true, arrayListOf("cd /data/openpilot/selfdrive/debug", "pkill -f boardd", "./clear_dtc.py",
                "sudo reboot")),
            CmdItem(R.string.shell_rebuild, true, arrayListOf("cd /data/openpilot", "scons -c", "rm .sconsign.dblite",
                "rm -rf /tmp/scons_cache", "rm prebuilt",
                "sudo reboot")),
            CmdItem(R.string.shell_reboot, true, arrayListOf("sudo reboot")),
        )

        var cmdList = if(Feature.is_tici()) cmdList_tici else cmdList_eon

        fun refesh() {
            cmdList = if(Feature.is_tici()) cmdList_tici else cmdList_eon
        }
    }

    private val jsch: JSch = JSch()
    private lateinit var session: Session
    private var channel: ChannelShell? = null
    private val handler = Handler(Looper.getMainLooper())
    private var queue: BlockingQueue<String> = LinkedBlockingQueue()

    init
    {
        var privateKey = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PRIVATE_KEY, "")
        var publicKey = ""

        if(privateKey.isEmpty())
        {
            privateKey = SshSession.privateKey
            publicKey = SshSession.publicKey
        }
        else
        {
            publicKey = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PUBLIC_KEY, "")
        }

        var passphrase = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PASSWORD_KEY, "")
        if(passphrase.isEmpty())
            passphrase = "passphrase"

        jsch.addIdentity(
            "id_rsa",
            privateKey.toByteArray(),
            publicKey.toByteArray(),
            passphrase.toByteArray()
        )

        session = jsch.getSession(if(Feature.is_tici()) "comma" else "root", host, port)
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        session.setConfig(config)
    }

    fun isConnected(): Boolean
    {
        return session.isConnected
    }

    override fun run() {

        try {

            session.connect(10000)

            channel = session.openChannel("shell") as ChannelShell
            channel?.let {
                it.connect(10000)
            }

            val bufferedReader = BufferedReader(InputStreamReader(channel?.inputStream))
            startSendThread()

            try
            {
                while(!isInterrupted)
                {
                    val line = bufferedReader.readLine() ?: break

                    if(line.isNotEmpty())
                    {
                        handler.post {
                            listener.onRead(line.replace(Regex("\\e\\[[\\d;]*[^\\d;]"),""))
                        }
                    }
                }
            }
            catch (e: Exception){}

            es?.shutdownNow()
        }
        catch (e: Exception)
        {
            //e.printStackTrace()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(MyApp.getContext(), e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private var es: ExecutorService? = null
    private fun startSendThread()
    {
        es = Executors.newCachedThreadPool()
        es?.execute {

            channel?.let {

                try {

                    val writer = PrintWriter(it.outputStream)

                    while(!isInterrupted)
                    {
                        val cmd = queue.take()
                        writer.println(cmd)
                        writer.flush()
                    }
                }
                catch (e: Exception){}
            }
        }
    }

    fun close()
    {
        channel?.disconnect()
        session.disconnect()
        interrupt()
        join(3000)
    }

    interface OnSshListener
    {
        fun onConnect()
        fun onError(e: Exception)
        fun onRead(res: String)
    }

    fun send(cmd: String)
    {
        queue.add(cmd)
        queue.add("")
    }


}