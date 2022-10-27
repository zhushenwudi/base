package com.zhushenwudi.base.wifi

interface IWifiManager {
    val isOpened: Boolean
    fun openWifi()
    fun closeWifi()
    fun scanWifi()
    fun disConnectWifi(): Boolean
    fun connectEncryptWifi(wifi: IWifi?, password: String?): Boolean
    fun connectSavedWifi(wifi: IWifi?): Boolean
    fun connectOpenWifi(wifi: IWifi?): Boolean
    fun removeWifi(wifi: IWifi?): Boolean
    val wifi: List<IWifi?>?
    fun setOnWifiConnectListener(onWifiConnectListener: OnWifiConnectListener?)
    fun setOnWifiStateChangeListener(onWifiStateChangeListener: OnWifiStateChangeListener?)
    fun setOnWifiChangeListener(onWifiChangeListener: OnWifiChangeListener?)
    fun destroy()
}