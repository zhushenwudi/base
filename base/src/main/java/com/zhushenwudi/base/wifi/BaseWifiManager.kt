package com.zhushenwudi.base.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.zhushenwudi.base.wifi.*
import com.zhushenwudi.base.wifi.Wifi.Companion.create
import com.zhushenwudi.base.wifi.WifiHelper.removeDuplicate

@SuppressLint("ServiceCast")
abstract class BaseWifiManager(private val context: Context): IWifiManager,
    WifiReceiver.IWifiReceiver {

    var manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    var wifis = mutableListOf<IWifi>()
    var mWifiChangeListener: OnWifiChangeListener? = null
    var mWifiConnectListener: OnWifiConnectListener? = null
    var mWifiStateChangeListener: OnWifiStateChangeListener? = null

    private var mHandler: Handler? = null

    var wifiReceiver = WifiReceiver()

    fun create(): BaseWifiManager {
        initReceiver(context)

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WifiState.WIFI_STATE_DISABLED -> {
                        mWifiStateChangeListener?.onStateChanged(State.DISABLED)
                    }
                    WifiState.WIFI_STATE_DISABLING -> {
                        mWifiStateChangeListener?.onStateChanged(State.DISABLING)
                    }
                    WifiState.WIFI_STATE_ENABLED -> {
                        mWifiStateChangeListener?.onStateChanged(State.ENABLED)
                    }
                    WifiState.WIFI_STATE_ENABLING -> {
                        mWifiStateChangeListener?.onStateChanged(State.ENABLING)
                    }
                    WifiState.WIFI_STATE_UNKNOWN -> {
                        mWifiStateChangeListener?.onStateChanged(State.UNKNOWN)
                    }
                    WifiState.WIFI_STATE_MODIFY -> {
                        mWifiChangeListener?.onWifiChanged(wifis)
                    }
                    WifiState.WIFI_STATE_CONNECTED -> {
                        val b = msg.data
                        val ssid = b.getString("ssid")
                        mWifiConnectListener?.onConnectChanged(status = true, connectedSSID = ssid)
                    }
                    WifiState.WIFI_STATE_UNCONNECTED -> {
                        mWifiConnectListener?.onConnectChanged(status = false, connectedSSID = null)
                    }
                }
            }
        }
        return this
    }

    private fun initReceiver(context: Context) {
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) //监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) //监听wifiwifi连接状态广播

        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) //监听wifi列表变化（开启一个热点或者关闭一个热点）

        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiReceiver, filter)
        wifiReceiver.setInterface(this@BaseWifiManager)
    }

    override fun receiverScanWifi() {
        scanWifi()
    }

    override fun receiverSendEmptyMsg(what: Int) {
        mHandler?.sendEmptyMessage(what)
    }

    override fun receiverSendMsg(message: Message) {
        mHandler?.sendMessage(message)
    }

    override fun receiverModifyList(ssid: String, state: String, isConnected: Boolean) {
        synchronized(wifis) {
            try {
                val wifiList: MutableList<IWifi> = ArrayList()
                for (wifi in wifis) {
                    if (ssid == wifi.SSID()) {
                        wifi.state(state)
                        wifi.isConnected = true
                        wifiList.add(0, wifi)
                    } else {
                        wifi.state(null)
                        wifi.isConnected = false
                        wifiList.add(wifi)
                    }
                }
                wifis.clear()
                wifis.addAll(wifiList)
                mHandler?.sendEmptyMessage(WifiState.WIFI_STATE_MODIFY)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun receiverUpdateWifiList() {
        synchronized(wifis) {
            try {
                val results = manager?.scanResults ?: return
                val wifiList = mutableListOf<IWifi>()
                var mergeList = mutableListOf<IWifi>()
                val configurations = manager?.configuredNetworks ?: return
                val connectedSSID = manager?.connectionInfo?.ssid ?: return
                val ipAddress = manager?.connectionInfo?.ipAddress ?: return
                for (result in results) {
                    val mergeObj = create(result!!, configurations, connectedSSID, ipAddress)
                        ?: continue
                    mergeList.add(mergeObj)
                }
                mergeList = removeDuplicate(mergeList)
                for (merge in mergeList) {
                    var isMerge = false
                    for (wifi in wifis) {
                        if (wifi == merge) {
                            wifi.merge(merge)?.run {
                                wifiList.add(this)
                                isMerge = true
                            }
                        }
                    }
                    if (!isMerge) wifiList.add(merge)
                }
                wifis.clear()
                wifis.addAll(wifiList)
                mHandler?.sendEmptyMessage(WifiState.WIFI_STATE_MODIFY)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setOnWifiConnectListener(onWifiConnectListener: OnWifiConnectListener?) {
        this.mWifiConnectListener = onWifiConnectListener
    }

    override fun setOnWifiStateChangeListener(onWifiStateChangeListener: OnWifiStateChangeListener?) {
        this.mWifiStateChangeListener = onWifiStateChangeListener
    }

    override fun setOnWifiChangeListener(onWifiChangeListener: OnWifiChangeListener?) {
        this.mWifiChangeListener = onWifiChangeListener
    }

    override fun receiverGetConnectionInfo(): WifiInfo? {
        return manager?.connectionInfo
    }

    override fun destroy() {
        try {
            context.unregisterReceiver(wifiReceiver)
        } catch (ignored: Exception) {
        }
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
        manager = null
    }

    companion object {
        const val TAG = "baseWifiManager"
    }
}