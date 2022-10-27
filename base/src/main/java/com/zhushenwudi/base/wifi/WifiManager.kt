package com.zhushenwudi.base.wifi

import android.content.Context
import com.zhushenwudi.base.wifi.WifiHelper.configOrCreateWifi
import com.zhushenwudi.base.wifi.WifiHelper.deleteWifiConfiguration
import com.zhushenwudi.base.wifi.BaseWifiManager
import dev.utils.LogPrintUtils

class WifiManager private constructor(context: Context) : BaseWifiManager(context) {
    override val isOpened: Boolean
        get() = manager?.isWifiEnabled ?: false
    override val wifi: List<IWifi?>
        get() = wifis

    override fun openWifi() {
        if (manager?.isWifiEnabled == false) {
            manager?.isWifiEnabled = true
        }
    }

    override fun closeWifi() {
        if (manager?.isWifiEnabled == true) {
            manager?.isWifiEnabled = false
        }
    }

    override fun scanWifi() {
        manager?.startScan()
    }

    override fun disConnectWifi(): Boolean {
        return manager?.disconnect() ?: false
    }

    override fun connectEncryptWifi(wifi: IWifi?, password: String?): Boolean {
        manager?.run {
            val ssid = wifi?.SSID() ?: return false
            if (connectionInfo != null && ssid == connectionInfo.ssid) return true
            val networkId = configOrCreateWifi(this, wifi, password)
            val ret = enableNetwork(networkId, true)
            receiverModifyList(ssid, "开始连接...", false)
            return ret
        }

        return false
    }

    override fun connectSavedWifi(wifi: IWifi?): Boolean {
        manager?.run {
            val ssid = wifi?.SSID() ?: return false
            val networkId = configOrCreateWifi(this, wifi, null)
            val ret = enableNetwork(networkId, true)
            receiverModifyList(ssid, "开始连接...", false)
            return ret
        }
        return false
    }

    override fun connectOpenWifi(wifi: IWifi?): Boolean {
        val ssid = wifi?.SSID() ?: return false
        val ret = connectEncryptWifi(wifi, null)
        receiverModifyList(ssid, "开始连接...", false)
        return ret
    }

    override fun removeWifi(wifi: IWifi?): Boolean {
        manager?.run {
            wifi?.let {
                val ret = deleteWifiConfiguration(this, it)
                receiverUpdateWifiList()
                return ret
            }
        }
        return false
    }

    companion object {
        fun create(context: Context): IWifiManager {
            return WifiManager(context).create()
        }
    }
}