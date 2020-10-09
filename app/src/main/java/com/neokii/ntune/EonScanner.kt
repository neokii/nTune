package com.neokii.ntune

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList


class EonScanner() {

    interface OnResultListener { fun onResult(ip: String?) }

    @Volatile
    private var address: String? = null

    fun portIsOpen(
        es: ExecutorService,
        ip: String,
        port: Int,
        timeout: Int,
        listener: OnResultListener?
    ): Future<String?> {
        return es.submit(Callable<String?> {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), timeout)

                val bufferedReader =  BufferedReader(InputStreamReader(socket.getInputStream(),"UTF-8"))
                val read = bufferedReader.readLine()

                socket.close()

                if(read.startsWith("SSH"))
                {
                    es.shutdownNow()
                    address = ip
                    listener?.let {
                        Handler(Looper.getMainLooper()).post {
                            listener.onResult(ip)
                        }
                    }
                }

                address

            } catch (ex: Exception) {
                null
            }
        })
    }

    private fun getIpAddress(): ArrayList<String>? {
        var ip = arrayListOf<String>()
        try {
            val enumNetworkInterfaces: Enumeration<NetworkInterface> = NetworkInterface
                .getNetworkInterfaces()
            while (enumNetworkInterfaces.hasMoreElements()) {
                val networkInterface: NetworkInterface = enumNetworkInterfaces
                    .nextElement()
                val enumInetAddress: Enumeration<InetAddress> = networkInterface
                    .getInetAddresses()
                while (enumInetAddress.hasMoreElements()) {
                    val inetAddress: InetAddress = enumInetAddress.nextElement()
                    if (inetAddress.isSiteLocalAddress()) {
                        ip.add(inetAddress.getHostAddress())
                    }
                }
            }
        } catch (e: SocketException) {
        }
        return ip
    }

    fun startScan(context: Context, port: Int, listener: OnResultListener?)
    {
        val ipList = getIpAddress()

        ipList?.let {

            Executors.newSingleThreadExecutor().execute{

                val es = Executors.newFixedThreadPool(50)
                val timeout = 2000
                val futures: MutableList<Future<String?>> = ArrayList()

                for (ipAddress in ipList)
                {
                    ipAddress.lastIndexOf(".").also { it ->
                        if(it > 0)
                        {
                            val ipBase = ipAddress.substring(0, it)
                            for (cls in 1..255) {
                                val ip = "$ipBase.$cls"
                                futures.add(portIsOpen(es, ip, port, timeout, listener))
                            }
                        }
                    }
                }

                es.shutdown()
                es.awaitTermination(10, TimeUnit.SECONDS)

                if(address == null)
                {
                    Handler(Looper.getMainLooper()).post {
                        listener?.onResult(null)
                    }
                }
            }
        }
    }
}