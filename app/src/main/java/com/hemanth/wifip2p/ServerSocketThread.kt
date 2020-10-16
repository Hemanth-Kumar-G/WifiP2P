package com.hemanth.wifip2p

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.ServerSocket

class ServerSocketThread : AsyncTask<Any?, Any?, Any?>() {

    companion object {
        val TAG: String = ServerSocketThread::class.java.simpleName
    }

    lateinit var serverSocket: ServerSocket
    var receivedData = "null"
    private val port = 8888
    var isInterrupted = false
    var listener: OnUpdateListener? = null

    interface OnUpdateListener {
        fun onUpdate(data: String?)
    }

    fun setUpdateListener(listener: OnUpdateListener?) {
        this.listener = listener
    }

    override fun doInBackground(vararg objects: Any?) {
        try {
            Log.d(TAG, " started DoInBackground")
            serverSocket = ServerSocket(port)
            while (!isInterrupted) {
                val client = serverSocket.accept()
                Log.d(TAG, "Accepted Connection")
                val inputstream = client.getInputStream()
                val bufferedReader = BufferedReader(InputStreamReader(inputstream))
                val sb = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                Log.d(TAG, "Completed ReceiveDataTask")
                receivedData = sb.toString()
                if (listener != null) {
                    listener!!.onUpdate(receivedData)
                }
                Log.d(TAG, " ================ $receivedData")
                serverSocket!!.close()
            }
        } catch (e: IOException) {
            Log.e(TAG, "doInBackground:  ${e.message}")
        }

    }
}