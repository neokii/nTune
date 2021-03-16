package com.neokii.ntune

import android.os.Handler
import android.os.Looper
import com.jcraft.jsch.*
import java.io.*
import java.util.*
import java.util.concurrent.Executors


class SshSession(val host: String, val port: Int) {

    companion object
    {
        val privateKey = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC+iXXq30Tq+J5N\n" +
                "Kat3KWHCzcmwZ55nGh6WggAqECa5CasBlM9VeROpVu3beA+5h0MibRgbD4DMtVXB\n" +
                "t6gEvZ8nd04E7eLA9LTZyFDZ7SkSOVj4oXOQsT0GnJmKrASW5KslTWqVzTfo2XCt\n" +
                "Z+004ikLxmyFeBO8NOcErW1pa8gFdQDToH9FrA7kgysic/XVESTOoe7XlzRoe/eZ\n" +
                "acEQ+jtnmFd21A4aEADkk00Ahjr0uKaJiLUAPatxs2icIXWpgYtfqqtaKF23wSt6\n" +
                "1OTu6cAwXbOWr3m+IUSRUO0IRzEIQS3z1jfd1svgzSgSSwZ1Lhj4AoKxIEAIc8qJ\n" +
                "rO4uymCJAgMBAAECggEBAISFevxHGdoL3Z5xkw6oO5SQKO2GxEeVhRzNgmu/HA+q\n" +
                "x8OryqD6O1CWY4037kft6iWxlwiLOdwna2P25ueVM3LxqdQH2KS4DmlCx+kq6FwC\n" +
                "gv063fQPMhC9LpWimvaQSPEC7VUPjQlo4tPY6sTTYBUOh0A1ihRm/x7juKuQCWix\n" +
                "Cq8C/DVnB1X4mGj+W3nJc5TwVJtgJbbiBrq6PWrhvB/3qmkxHRL7dU2SBb2iNRF1\n" +
                "LLY30dJx/cD73UDKNHrlrsjk3UJc29Mp4/MladKvUkRqNwlYxSuAtJV0nZ3+iFkL\n" +
                "s3adSTHdJpClQer45R51rFDlVsDz2ZBpb/hRNRoGDuECgYEA6A1EixLq7QYOh3cb\n" +
                "Xhyh3W4kpVvA/FPfKH1OMy3ONOD/Y9Oa+M/wthW1wSoRL2n+uuIW5OAhTIvIEivj\n" +
                "6bAZsTT3twrvOrvYu9rx9aln4p8BhyvdjeW4kS7T8FP5ol6LoOt2sTP3T1LOuJPO\n" +
                "uQvOjlKPKIMh3c3RFNWTnGzMPa0CgYEA0jNiPLxP3A2nrX0keKDI+VHuvOY88gdh\n" +
                "0W5BuLMLovOIDk9aQFIbBbMuW1OTjHKv9NK+Lrw+YbCFqOGf1dU/UN5gSyE8lX/Q\n" +
                "FsUGUqUZx574nJZnOIcy3ONOnQLcvHAQToLFAGUd7PWgP3CtHkt9hEv2koUwL4vo\n" +
                "ikTP1u9Gkc0CgYEA2apoWxPZrY963XLKBxNQecYxNbLFaWq67t3rFnKm9E8BAICi\n" +
                "4zUaE5J1tMVi7Vi9iks9Ml9SnNyZRQJKfQ+kaebHXbkyAaPmfv+26rqHKboA0uxA\n" +
                "nDOZVwXX45zBkp6g1sdHxJx8JLoGEnkC9eyvSi0C//tRLx86OhLErXwYcNkCf1it\n" +
                "VMRKrWYoXJTUNo6tRhvodM88UnnIo3u3CALjhgU4uC1RTMHV4ZCGBwiAOb8GozSl\n" +
                "s5YD1E1iKwEULloHnK6BIh6P5v8q7J6uf/xdqoKMjlWBHgq6/roxKvkSPA1DOZ3l\n" +
                "jTadcgKFnRUmc+JT9p/ZbCxkA/ALFg8++G+0ghECgYA8vG3M/utweLvq4RI7l7U7\n" +
                "b+i2BajfK2OmzNi/xugfeLjY6k2tfQGRuv6ppTjehtji2uvgDWkgjJUgPfZpir3I\n" +
                "RsVMUiFgloWGHETOy0Qvc5AwtqTJFLTD1Wza2uBilSVIEsg6Y83Gickh+ejOmEsY\n" +
                "6co17RFaAZHwGfCFFjO76Q==\n" +
                "-----END PRIVATE KEY-----"

        val publicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC+iXXq30Tq+J5NKat3KWHCzcmwZ55nGh6WggAqECa5CasBlM9VeROpVu3beA+5h0MibRgbD4DMtVXBt6gEvZ8nd04E7eLA9LTZyFDZ7SkSOVj4oXOQsT0GnJmKrASW5KslTWqVzTfo2XCtZ+004ikLxmyFeBO8NOcErW1pa8gFdQDToH9FrA7kgysic/XVESTOoe7XlzRoe/eZacEQ+jtnmFd21A4aEADkk00Ahjr0uKaJiLUAPatxs2icIXWpgYtfqqtaKF23wSt61OTu6cAwXbOWr3m+IUSRUO0IRzEIQS3z1jfd1svgzSgSSwZ1Lhj4AoKxIEAIc8qJrO4uymCJ imported-openssh-key"

    }


    private val jsch: JSch = JSch()
    private lateinit var session: Session

    init
    {
        var privateKey = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PRIVATE_KEY, "")
        if(privateKey.isEmpty())
            privateKey = SshSession.privateKey

        var publicKey = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PUBLIC_KEY, "")
        if(publicKey.isEmpty())
            publicKey = SshSession.publicKey

        var passphrase = SettingUtil.getString(MyApp.getContext(), SshKeySettingActivity.PREF_PASSWORD_KEY, "")
        if(passphrase.isEmpty())
            passphrase = "passphrase"

        jsch.addIdentity(
            "id_rsa",
            privateKey.toByteArray(),
            publicKey.toByteArray(),
            passphrase.toByteArray()
        )
    }

    interface OnConnectListener { fun onConnect() fun onFail(e: Exception) }
    interface OnResponseListener { fun onResponse(res: String) fun onEnd(e: Exception?) }

    fun connect(listener: OnConnectListener?)
    {
        Executors.newSingleThreadExecutor().execute {

            try {
                session = jsch.getSession("root", host, port)
                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                session.setConfig(config)

                session.connect(5000)

                Handler(Looper.getMainLooper()).post {
                    listener?.onConnect()
                }
            }
            catch (e: Exception)
            {
                Handler(Looper.getMainLooper()).post {
                    listener?.onFail(e)
                }
            }
        }
    }

    fun exec(command: String, listener: OnResponseListener?)
    {
        Executors.newSingleThreadExecutor().execute {

            try {
                val outputStream = ByteArrayOutputStream(1024 * 32)
                val channel = session.openChannel("exec") as ChannelExec
                channel.setCommand(command)
                channel.outputStream = outputStream
                channel.connect()
                waitUntilChannelClosed(channel)
                channel.disconnect()

                Handler(Looper.getMainLooper()).post {
                    listener?.onResponse(outputStream.toString("UTF-8"))
                }

                Handler(Looper.getMainLooper()).post {
                    listener?.onEnd(null)
                }
            }
            catch (e: Exception)
            {
                Handler(Looper.getMainLooper()).post {
                    listener?.onEnd(e)
                }
            }
        }
    }

    class LogOutputStream(private val listener: OnResponseListener) : OutputStream()
    {
        private val handler = Handler(Looper.getMainLooper())

        @Throws(IOException::class)
        override fun write(b: Int) {
            val s = b.toChar().toString()
            handler.post{
                listener.onResponse(s)
            }
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray?, off: Int, len: Int) {
            val s = String(b!!, off, len)
            handler.post{
                listener.onResponse(s)
            }
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray) {
            this.write(b, 0, b.size)
        }

    }

    fun shell(command: String, listener: OnResponseListener)
    {
        Executors.newSingleThreadExecutor().execute {

            try {

                val channel = session.openChannel("exec") as ChannelExec

                channel.outputStream = LogOutputStream(listener)

                channel.setCommand(command)
                channel.connect()

                waitUntilChannelClosed(channel)
                channel.disconnect()

                Handler(Looper.getMainLooper()).post {
                    listener.onEnd(null)
                }
            }
            catch (e: Exception)
            {
                Handler(Looper.getMainLooper()).post {
                    listener.onEnd(e)
                }
            }
        }
    }

    fun close()
    {
        session.disconnect()
    }

    private fun waitUntilChannelClosed(executionChannel: Channel) {
        var waitTimeThusFar = 0L
        val sessionTimeout: Long = 10000L
        do {
            try {
                Thread.sleep(100L)
                waitTimeThusFar += 100L
                if (sessionTimeout > 0L && waitTimeThusFar > sessionTimeout) {
                    break
                }
            } catch (e: InterruptedException) {
            }
        } while (!executionChannel.isClosed)
        if (!executionChannel.isClosed) {
            executionChannel.disconnect()
            throw Exception("Timeout: ${sessionTimeout}")
        }
    }
}