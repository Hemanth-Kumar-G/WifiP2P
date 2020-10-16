package com.hemanth.wifip2p

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hemanth.wifip2p.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener,
    WifiP2pManager.ConnectionInfoListener {

    companion object {
        const val REQUEST_PERMISSION_CODE = 101
        val TAG: String = MainActivity::class.java.simpleName
        var IP: String? = null
        var IS_OWNER = false
        var stateConnection = false
        var stateDiscovery = false
        var stateWifi = false
    }


    private lateinit var binding: ActivityMainBinding
    private lateinit var mManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private lateinit var mReceiver: WifiBroadcastReceiver
    private lateinit var mIntentFilter: IntentFilter
    private var device: WifiP2pDevice? = null
    private lateinit var serverSocketThread: ServerSocketThread
    private val list = ArrayList<String>()
    private lateinit var adapter: ReceivedDataAdapter

    var mAdapter: ArrayAdapter<*>? = null
    lateinit var deviceListItems: Array<WifiP2pDevice?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        checkLocationPermission()
        initClick()
        init()
        initRv()
    }

    private fun initRv() {
        adapter = ReceivedDataAdapter(list)
        binding.rvReceivedData.adapter = adapter
    }

    private fun init() {
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(this, mainLooper, null)
        mReceiver = WifiBroadcastReceiver(mManager, mChannel, this)
        serverSocketThread = ServerSocketThread()
    }

    private fun initClick() {
        binding.btnStartDiscovery.setOnClickListener(this)
        binding.btnStopDiscovery.setOnClickListener(this)
        binding.btnConnect.setOnClickListener(this)
        binding.btnConfigure.setOnClickListener(this)
        binding.btnStartServer.setOnClickListener(this)
        binding.btnStopServer.setOnClickListener(this)
        binding.btnSendString.setOnClickListener(this)
        binding.btnStopClient.setOnClickListener(this)
        binding.rvAvailableDevice.onItemClickListener =
            OnItemClickListener { _, _, i, _ ->
                device = deviceListItems[i]
                Toast.makeText(
                    this@MainActivity, "Selected device :" + device!!.deviceName, Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        setUpIntentFilter()
        registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }

    private fun setUpIntentFilter() {
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    @SuppressLint("MissingPermission")
    fun discoverPeers() {
        Log.d(TAG, "discoverPeers()")
        setDeviceList(ArrayList<WifiP2pDevice>())
        mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                stateDiscovery = true
                Log.d(TAG, "peer discovery started")
                makeToast("peer discovery started")
                val myPeerListener = MyPeerListener(this@MainActivity)
                mManager.requestPeers(mChannel, myPeerListener)
            }

            override fun onFailure(i: Int) {
                stateDiscovery = false
                when (i) {
                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        Log.d(TAG, " peer discovery failed :" + "P2P_UNSUPPORTED")
                        makeToast(" peer discovery failed :" + "P2P_UNSUPPORTED")
                    }
                    WifiP2pManager.ERROR -> {
                        Log.d(TAG, " peer discovery failed :" + "ERROR")
                        makeToast(" peer discovery failed :" + "ERROR")
                    }
                    WifiP2pManager.BUSY -> {
                        Log.d(TAG, " peer discovery failed :" + "BUSY")
                        makeToast(" peer discovery failed :" + "BUSY")
                    }
                }
            }
        })
    }

    private fun stopPeerDiscover() {
        mManager.stopPeerDiscovery(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                stateDiscovery = false
                Log.d(TAG, "Peer Discovery stopped")
                makeToast("Peer Discovery stopped")
            }

            override fun onFailure(i: Int) {
                Log.d(TAG, "Stopping Peer Discovery failed")
                makeToast("Stopping Peer Discovery failed")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connect(device: WifiP2pDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC
        Log.d(TAG, "Trying to connect : ${device.deviceName}")
        mManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connected to :${device.deviceName}")
                makeToast("Connection successful with ${device.deviceName}")
            }

            override fun onFailure(reason: Int) {
                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(TAG, "P2P_UNSUPPORTED")
                    makeToast("Failed establishing connection:         P2P_UNSUPPORTED")
                } else if (reason == WifiP2pManager.ERROR) {
                    Log.d(TAG, "Connection failed : ERROR")
                    makeToast("Failed establishing connection:       ERROR")
                } else if (reason == WifiP2pManager.BUSY) {
                    Log.d(TAG, "Connection failed : BUSY")
                    makeToast("Failed establishing connection:        BUSY")
                }
            }
        })
    }

    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_CODE
            )
        } else {
//            finish()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CODE
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult: ")
        }
    }

    fun setDeviceList(deviceDetails: ArrayList<WifiP2pDevice>) {
        deviceListItems =
            arrayOfNulls(size = deviceDetails.size)
//        deviceListItems = arrayOfNulls<WifiP2pDevice>(size = deviceDetails.size)
        val deviceNames =
            arrayOfNulls<String>(deviceDetails.size)
        for (i in deviceDetails.indices) {
            deviceNames[i] = deviceDetails[i].deviceName
            deviceListItems[i] = deviceDetails[i]
        }
        mAdapter = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            deviceNames
        )
        binding.rvAvailableDevice.adapter = mAdapter
        Log.e(TAG, "setDeviceList: $deviceDetails")
    }

    fun setStatusView(status: Constants) {
        when (status) {
            Constants.DISCOVERY_INITATITED -> {
                stateDiscovery = true
                binding.tvDiscoveryStatus.text = "DISCOVERY_INITIATED"
            }
            Constants.DISCOVERY_STOPPED -> {
                stateDiscovery = false
                binding.tvDiscoveryStatus.text = "DISCOVERY_STOPPED"
            }
            Constants.P2P_WIFI_DISABLED -> {
                stateWifi = false
                binding.tvWifiP2PStatus.text = "P2P_WIFI_DISABLED"
                binding.btnStartDiscovery.isEnabled = false
                binding.btnStopDiscovery.isEnabled = false
            }
            Constants.P2P_WIFI_ENABLED -> {
                stateWifi = true
                binding.tvWifiP2PStatus.text = "P2P_WIFI_ENABLED"
                binding.btnStartDiscovery.isEnabled = true
                binding.btnStopDiscovery.isEnabled = true
            }
            Constants.NETWORK_CONNECT -> {
                stateConnection = true
                makeToast("It's a connect")
                binding.tvConnectionStatus.text = "Connected"
            }
            Constants.NETWORK_DISCONNECT -> {
                stateConnection = false
                binding.tvConnectionStatus.text = "Disconnected"
                makeToast("State is disconnected")
            }
            else -> Log.d(TAG, "Unknown status")
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnStartDiscovery -> if (!stateDiscovery) {
                discoverPeers()
            }
            R.id.btnStopDiscovery -> if (stateDiscovery) {
                stopPeerDiscover()
            }
            R.id.btnConnect -> {
                if (device == null) {
                    makeToast("Please discover and select a device")
                    return
                }
                connect(device!!)
            }
            R.id.btnStartServer -> {
                serverSocketThread = ServerSocketThread()
                serverSocketThread.setUpdateListener(object :
                    ServerSocketThread.OnUpdateListener {
                    override fun onUpdate(obj: String?) {
                        setReceivedText(obj)
                    }
                })
                serverSocketThread.execute()
            }
            R.id.btnStopServer -> if (serverSocketThread != null) {
                serverSocketThread.isInterrupted = true
            } else {
                Log.d(TAG, "serverSocketThread is null")
            }
            R.id.btnSendString -> {
                //serviceDisvcoery.startRegistrationAndDiscovery(mManager,mChannel);
                val dataToSend: String = binding.etSendData.text.toString()
                val clientSocket = ClientSocket(dataToSend)
                clientSocket.execute()
            }
            R.id.btnConfigure -> mManager.requestConnectionInfo(mChannel, this)
            R.id.btnStopServer -> makeToast("Yet to do")
            else -> {
            }
        }
    }

    fun setReceivedText(data: String?) {
        runOnUiThread { /*textViewReceivedData.setText(data)*/
            makeToast(data)
            list.add(data!!)
            adapter.notifyDataSetChanged()
            binding.rvReceivedData.scrollToPosition(list.size)
        }
    }

    fun makeToast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo?) {
        var hostAddress: String? = wifiP2pInfo?.groupOwnerAddress?.getHostAddress()
        if (hostAddress == null) hostAddress = "host is null"

        makeToast("Am I group owner : " + (wifiP2pInfo?.isGroupOwner).toString())
        makeToast(hostAddress);
        Log.d(
            TAG,
            "wifiP2pInfo.groupOwnerAddress.getHostAddress() " + wifiP2pInfo!!.groupOwnerAddress.hostAddress
        )
        IP = wifiP2pInfo.groupOwnerAddress.hostAddress
        IS_OWNER = wifiP2pInfo.isGroupOwner

        binding.grpNotOwner.visibility = if (IS_OWNER) View.GONE else View.VISIBLE
        binding.grpOwner.visibility = if (!IS_OWNER) View.GONE else View.VISIBLE

        makeToast("Configuration Completed")
    }
}