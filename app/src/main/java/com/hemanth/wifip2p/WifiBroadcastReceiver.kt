package com.hemanth.wifip2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

class WifiBroadcastReceiver(
    private val mManager: WifiP2pManager?,
    private val mChannel: WifiP2pManager.Channel,
    private val mActivity: MainActivity
) : BroadcastReceiver() {
    companion object {
        val TAG: String = WifiBroadcastReceiver::class.java.simpleName
    }


    @SuppressLint("MissingPermission")
    override fun onReceive(p0: Context?, intent: Intent?) {

        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "WIFI P2P ENABLED")
                    mActivity.setStatusView(Constants.P2P_WIFI_ENABLED)
                } else {
                    Log.d(TAG, "WIFI P2P NOT ENABLED")
                    mActivity.setStatusView(Constants.P2P_WIFI_DISABLED)
                }
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
                if (mManager != null) {
                    val myPeerListener = MyPeerListener(mActivity)
                    mManager.requestPeers(mChannel, myPeerListener)
                }

            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                if (mManager == null) return
                val networkInfo = intent
                    .getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

                if (networkInfo!!.isConnected) {
                    mActivity.setStatusView(Constants.NETWORK_CONNECT)
                    // we are connected with the other device, request connection
                    // info to find group owner IP
                    //mManager.requestConnectionInfo(mChannel, mActivity);
                } else {
                    // It's a disconnect
                    Log.d(TAG, "Its a disconnect")
                    mActivity.setStatusView(Constants.NETWORK_DISCONNECT)
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
            }

            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000)
                if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    mActivity.setStatusView(Constants.DISCOVERY_INITATITED)
                } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                    mActivity.setStatusView(Constants.DISCOVERY_STOPPED)
                }
            }
        }
    }
}