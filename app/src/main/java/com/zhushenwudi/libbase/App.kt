package com.zhushenwudi.libbase

import com.zhushenwudi.base.app.BaseApp
import com.zhushenwudi.base.mvvm.m.Bridge
import com.zhushenwudi.base.mvvm.m.DingTalkBean
import com.zhushenwudi.base.mvvm.m.Handle
import com.zhushenwudi.base.mvvm.m.MailBean
import com.zhushenwudi.base.utils.getDeviceSN

val appVM: MyAppVM by lazy { App.appViewModelInstance }

class App: BaseApp(
    Bridge(
        isDebug = BuildConfig.DEBUG,
        crashHandler = Handle.NO,
        versionName = BuildConfig.VERSION_NAME,
        serialNo = getDeviceSN(),
        restartActivity = MainActivity::class.java
    )
) {

    companion object {
        lateinit var appViewModelInstance: MyAppVM
    }

    override fun onCreate() {
        super.onCreate()
        appViewModelInstance = getAppViewModelProvider()[MyAppVM::class.java]
    }
}