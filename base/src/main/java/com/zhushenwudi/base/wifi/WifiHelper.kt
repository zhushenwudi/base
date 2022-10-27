package com.zhushenwudi.base.wifi

import android.annotation.SuppressLint
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import java.lang.Exception
import java.util.*

object WifiHelper {
    const val WEP = "WEP"
    const val PSK = "PSK"
    const val EAP = "EAP"
    const val WPA = "WPA"
    @JvmStatic
    @SuppressLint("MissingPermission")
    fun configOrCreateWifi(manager: WifiManager, wifi: IWifi, password: String?): Int {
        return try {
            val configurations = manager.configuredNetworks
            for (configuration in configurations) {
                if (configuration.SSID == wifi.SSID()) return configuration.networkId
            }
            val configuration = createWifiConfiguration(wifi, password)
            saveWifiConfiguration(manager, configuration)
        } catch (e: Exception) {
            -1
        }
    }

    @JvmStatic
    @SuppressLint("MissingPermission")
    fun deleteWifiConfiguration(manager: WifiManager, wifi: IWifi): Boolean {
        return try {
            val configurations = manager.configuredNetworks
            for (configuration in configurations) {
                if (configuration.SSID == wifi.SSID()) {
                    var ret = manager.removeNetwork(configuration.networkId)
                    ret = ret and manager.saveConfiguration()
                    return ret
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun createWifiConfiguration(wifi: IWifi, password: String?): WifiConfiguration {
        val configuration = WifiConfiguration()
        if (password == null) {
            configuration.hiddenSSID = false
            configuration.status = WifiConfiguration.Status.ENABLED
            configuration.SSID = wifi.SSID()
            if (wifi.capabilities()!!.contains(WEP)) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                configuration.wepTxKeyIndex = 0
                configuration.wepKeys[0] = ""
            } else if (wifi.capabilities()!!.contains(PSK)) {
                configuration.preSharedKey = ""
            } else if (wifi.capabilities()!!.contains(EAP)) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                configuration.preSharedKey = ""
            } else {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                configuration.preSharedKey = null
            }
        } else {
            configuration.allowedAuthAlgorithms.clear()
            configuration.allowedGroupCiphers.clear()
            configuration.allowedKeyManagement.clear()
            configuration.allowedPairwiseCiphers.clear()
            configuration.allowedProtocols.clear()
            configuration.SSID = wifi.SSID()
            if (wifi.capabilities()!!.contains(WEP)) {
                configuration.preSharedKey = "\"" + password + "\""
                configuration.hiddenSSID = true
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                configuration.wepTxKeyIndex = 0
            } else if (wifi.capabilities()!!.contains(WPA)) {
                configuration.hiddenSSID = true
                configuration.preSharedKey = "\"" + password + "\""
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            } else {
                configuration.wepKeys[0] = ""
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                configuration.wepTxKeyIndex = 0
            }
        }
        return configuration
    }

    private fun saveWifiConfiguration(manager: WifiManager, configuration: WifiConfiguration): Int {
        val networkId = manager.addNetwork(configuration)
        manager.saveConfiguration()
        return networkId
    }

    @JvmStatic
    fun removeDuplicate(list: MutableList<IWifi>): MutableList<IWifi> {
        list.sortWith { l: IWifi, r: IWifi -> r.level() - l.level() }
        val set: MutableList<IWifi> = ArrayList()
        for (wifi in list) {
            if (!set.contains(wifi)) {
                if (wifi.isConnected) set.add(0, wifi) else set.add(wifi)
            }
        }
        return set
    }
}