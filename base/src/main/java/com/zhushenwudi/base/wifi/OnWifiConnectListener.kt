package com.zhushenwudi.base.wifi

interface OnWifiConnectListener {
    fun onConnectChanged(status: Boolean, connectedSSID: String?)
}