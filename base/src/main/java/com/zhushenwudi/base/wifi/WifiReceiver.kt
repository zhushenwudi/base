package com.zhushenwudi.base.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import androidx.annotation.RequiresApi

class WifiReceiver : BroadcastReceiver() {

    var iWifiReceiver: IWifiReceiver? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        // 监听wifi的打开与关闭，与wifi的连接无关
        val action = intent.action
        if (WifiManager.WIFI_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            var what = 0
            when (state) {
                WifiManager.WIFI_STATE_DISABLED -> {
                    what = WifiState.WIFI_STATE_DISABLED
                }
                WifiManager.WIFI_STATE_DISABLING -> {
                    what = WifiState.WIFI_STATE_DISABLING
                }
                WifiManager.WIFI_STATE_ENABLED -> {
                    what = WifiState.WIFI_STATE_ENABLED
                }
                WifiManager.WIFI_STATE_ENABLING -> {
                    what = WifiState.WIFI_STATE_ENABLING
                }
                WifiManager.WIFI_STATE_UNKNOWN -> {
                    what = WifiState.WIFI_STATE_UNKNOWN
                }
            }
            iWifiReceiver?.receiverSendEmptyMsg(what)
        }
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION == action) {
            val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                ?: return
            info.state ?: return
            val wifiInfo: WifiInfo = iWifiReceiver?.receiverGetConnectionInfo() ?: return
            var SSID = wifiInfo.ssid
            if (TextUtils.isEmpty(SSID)) return
            SSID = SSID.replace("\"", "") //去除双引号
            if (NetworkInfo.State.DISCONNECTED == info.state) { //wifi没连接上
                iWifiReceiver?.receiverSendEmptyMsg(WifiState.WIFI_STATE_UNCONNECTED)
                iWifiReceiver?.receiverModifyList(SSID, "已断开", false)
            } else if (NetworkInfo.State.CONNECTED == info.state) { //wifi连接上了
                val b = Bundle()
                b.putString("ssid", SSID)
                val message = Message()
                message.data = b
                message.what = WifiState.WIFI_STATE_CONNECTED
                iWifiReceiver?.receiverSendMsg(message)
                iWifiReceiver?.receiverModifyList(SSID, "已连接", true)
                iWifiReceiver?.receiverScanWifi()
            } else if (NetworkInfo.State.CONNECTING == info.state) { //正在连接
                iWifiReceiver?.receiverModifyList(SSID, "连接中...", false)
            } else if (NetworkInfo.State.DISCONNECTING == info.state) {
                iWifiReceiver?.receiverModifyList(SSID, "断开中...", false)
            }
        }
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == action) {
            val isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (isUpdated) iWifiReceiver?.receiverUpdateWifiList()

        }
    }

    fun setInterface(iWifiReceiver: IWifiReceiver) {
        this.iWifiReceiver = iWifiReceiver
    }

    interface IWifiReceiver {
        fun receiverSendEmptyMsg(what: Int)

        fun receiverSendMsg(message: Message)

        fun receiverModifyList(ssid: String, state: String, isConnected: Boolean)

        fun receiverScanWifi()

        fun receiverUpdateWifiList()

        fun receiverGetConnectionInfo(): WifiInfo?
    }
}