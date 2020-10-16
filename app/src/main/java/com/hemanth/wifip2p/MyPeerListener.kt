package com.hemanth.wifip2p

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import java.util.*

class MyPeerListener(private val mainActivity: MainActivity) : WifiP2pManager.PeerListListener {
    companion object {
        val TAG: String = MyPeerListener::class.java.simpleName
    }

    init {
        Log.d(TAG, "MyPeerListener object created")
    }

    private var peers: ArrayList<WifiP2pDevice> = ArrayList()

    override fun onPeersAvailable(wifiP2pDeviceList: WifiP2pDeviceList?) {
        val deviceDetails = ArrayList<WifiP2pDevice>()
        Log.d(TAG, "OnPeerAvailable")
        wifiP2pDeviceList?.let {
            if (it.deviceList.isEmpty())
                Log.d(TAG, "wifiP2pDeviceList size is zero")

            it.deviceList.forEach { wifiP2pDevice ->
                deviceDetails.add(wifiP2pDevice)
                Log.d(
                    TAG,
                    "Found device :" + wifiP2pDevice.deviceName + " " + wifiP2pDevice.deviceAddress
                )
            }

            mainActivity?.let { it ->
                it.setDeviceList(deviceDetails)
            }
        }
    }
}
