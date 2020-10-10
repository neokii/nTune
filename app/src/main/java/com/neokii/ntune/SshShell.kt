package com.neokii.ntune

import android.os.Handler
import android.os.Looper
import android.util.Log
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
        val title: String,
        val confirm: Boolean,
        val cmds: ArrayList<String>
    )

    companion object {
        val cmdList = arrayListOf(

            CmdItem("touch prebuilt", false, arrayListOf("touch /data/openpilot/prebuilt")),
            CmdItem("rm prebuilt", false, arrayListOf("rm /data/openpilot/prebuilt")),
            CmdItem("git reset --hard", true, arrayListOf("cd /data/openpilot && git reset --hard")),
            CmdItem("git pull", false, arrayListOf("cd /data/openpilot && git pull")),
            CmdItem("android settings", false, arrayListOf("am start -a android.settings.SETTINGS")),
            CmdItem("kill android settings", false, arrayListOf("pkill -f com.android.settings")),
            CmdItem("flash panda", true, arrayListOf("cd /data/openpilot/panda/board && pkill -f boardd && make")),
            CmdItem("reboot", true, arrayListOf("reboot"))

        )
    }

    private val jsch: JSch = JSch()
    private lateinit var session: Session
    private var channel: ChannelShell? = null
    private val handler = Handler(Looper.getMainLooper())
    private var queue: BlockingQueue<String> = LinkedBlockingQueue()

    init
    {
        jsch.addIdentity(
            "id_rsa",
            SshSession.privateKey.toByteArray(),
            SshSession.publicKey.toByteArray(),
            "passphrase".toByteArray()
        )

        session = jsch.getSession("root", host, port)
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

            session.connect(5000)

            channel = session.openChannel("shell") as ChannelShell
            channel?.let {
                it.connect(5000)
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
        catch (e: Exception){}
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