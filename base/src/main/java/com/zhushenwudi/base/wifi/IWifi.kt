package com.zhushenwudi.base.wifi

interface IWifi {
    fun name(): String?
    var isEncrypt: Boolean
    var isSaved: Boolean
    var isConnected: Boolean
    fun encryption(): String?
    fun level(): Int
    fun description(): String?
    fun ip(): String?
    fun description2(): String?
    fun state(state: String?)

    fun SSID(): String?

    fun capabilities(): String?

    fun merge(merge: IWifi?): IWifi?
    fun state(): String?
}