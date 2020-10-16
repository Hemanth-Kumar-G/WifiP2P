package com.hemanth.wifip2p

import android.os.AsyncTask
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket


class ClientSocket(private val data: String?) : AsyncTask<Unit, Unit, Any?>() {
    companion object {
        val TAG: String = ClientSocket::class.java.simpleName
    }

    lateinit var socket: Socket

    override fun doInBackground(vararg p0: Unit) {
        sendData()
    }

    override fun onPostExecute(o: Any?) {
        Log.d(TAG, "SendDataTask Completed")
    }

    fun sendData() {
        val host = MainActivity.IP
        val port = 8888
        var len: Int
        socket = Socket()
        val buf = ByteArray(1024)
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null)
            Log.d(TAG, "Trying to connect...")
            socket.connect(InetSocketAddress(host, port), 500)
            Log.d(TAG, "Connected...")
            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            val outputStream = socket.getOutputStream()
            val inputStream: InputStream?
            inputStream = ByteArrayInputStream(data!!.toByteArray())
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: FileNotFoundException) {
            Log.d(TAG, e.toString())
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }


        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        finally {
            if (socket.isConnected) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "sendData: ${e.message}")
                }
            }
        }
    }

}