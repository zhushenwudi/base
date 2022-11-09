package com.zhushenwudi.base.wifi

import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.text.TextUtils
import java.util.*

open class Wifi : IWifi {
    protected var name: String? = null
    protected var SSID: String? = null
    override var isEncrypt = false
    override var isSaved = false
    override var isConnected = false
    protected var encryption: String? = null
    protected var description: String? = null
    protected var capabilities: String? = null
    protected var ip: String? = null
    protected var state: String? = null
    protected var level = 0

    override fun name(): String? {
        return name
    }

    override fun encryption(): String? {
        return encryption
    }

    override fun level(): Int {
        return level
    }

    override fun description(): String? {
        return if (state == null) description else state
    }

    override fun ip(): String? {
        return ip
    }

    override fun description2(): String? {
        return if (isConnected) String.format("%s(%s)", description(), ip) else description()
    }

    override fun state(state: String?) {
        this.state = state
    }

    override fun SSID(): String? {
        return SSID
    }

    override fun capabilities(): String? {
        return capabilities
    }

    override fun merge(merge: IWifi?): IWifi? {
        merge?.run {
            isSaved = merge.isSaved
            isConnected = merge.isConnected
            ip = merge.ip()
            state = merge.state()
            level = merge.level()
            description = (merge as Wifi?)?.description
            return this
        }
        return null
    }

    override fun state(): String? {
        return state
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is Wifi) false else other.SSID == SSID
    }

    override fun toString(): String {
        return "{" +
                "\"name\":'" + name + "'" +
                ", \"SSID\":'" + SSID + "'" +
                ", \"isEncrypt\":" + isEncrypt +
                ", \"isSaved\":" + isSaved +
                ", \"isConnected\":" + isConnected +
                ", \"encryption\":'" + encryption + "'" +
                ", \"description\":'" + description + "'" +
                ", \"capabilities\":'" + capabilities + "'" +
                ", \"ip\":'" + ip + "'" +
                ", \"state\":'" + state + "'" +
                ", \"level\":" + level +
                '}'
    }

    companion object {
        @JvmStatic
        fun create(
            result: ScanResult,
            configurations: List<WifiConfiguration>,
            connectedSSID: String,
            ipAddress: Int
        ): IWifi? {
            if (TextUtils.isEmpty(result.SSID)) return null
            val wifi = Wifi()
            wifi.isConnected = false
            wifi.isSaved = false
            wifi.name = result.SSID
            wifi.SSID = "\"" + result.SSID + "\""
            wifi.isConnected = wifi.SSID == connectedSSID
            wifi.capabilities = result.capabilities
            wifi.isEncrypt = true
            wifi.encryption = ""
            wifi.level = result.level
            wifi.ip = if (wifi.isConnected) String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            ) else ""

            wifi.capabilities?.run {
                val encryptType = uppercase(Locale.getDefault())
                when {
                    encryptType.contains("WPA2-PSK") && encryptType.contains("WPA-PSK") -> wifi.encryption = "WPA/WPA2"
                    encryptType.contains("WPA-PSK") -> wifi.encryption = "WPA"
                    encryptType.contains("WPA2-PSK") -> wifi.encryption = "WPA2"
                    else -> wifi.isEncrypt = false
                }
            }?: run { wifi.isEncrypt = false }

            wifi.description = wifi.encryption
            for (configuration in configurations) {
                if (configuration.SSID == wifi.SSID) {
                    wifi.isSaved = true
                    break
                }
            }
            if (wifi.isSaved) {
                wifi.description = "已保存"
            }
            if (wifi.isConnected) {
                wifi.description = "已连接"
            }
            return wifi
        }
    }
}