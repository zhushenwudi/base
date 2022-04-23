package com.zhushenwudi.base.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.alley.openssl.OpensslUtil
import com.arialyy.aria.core.Aria
import com.zhushenwudi.base.mvvm.m.Bridge
import com.zhushenwudi.base.mvvm.m.Handle
import com.zhushenwudi.base.mvvm.v.ErrorActivity
import com.zhushenwudi.base.utils.DateUtils
import com.zhushenwudi.base.utils.SendUtil.sendDingTalk
import com.zhushenwudi.base.utils.SendUtil.sendMail
import com.zhushenwudi.base.utils.SpUtils
import com.zhushenwudi.base.utils.restartApplication
import dev.utils.LogPrintUtils
import dev.utils.app.AppUtils
import me.jessyan.autosize.AutoSizeConfig
import xcrash.ICrashCallback
import xcrash.TombstoneManager
import xcrash.TombstoneParser
import xcrash.XCrash

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/14
 * 描述　: 对于写BaseApp，其实我是拒绝的，但是需要提供一个很有用的功能--在Activity/fragment中获取Application级别的ViewModel
 * 所以才硬着头皮加的，如果你不想继承BaseApp，又想获取Application级别的ViewModel功能
 * 那么你可以复制该类的代码到你的自定义Application中去，然后可以自己写获取ViewModel的拓展函数即 :
 * GetViewModelExt类的getAppViewModel方法
 */

open class BaseApp(val bridge: Bridge) : Application(), ViewModelStoreOwner {

    private lateinit var mAppViewModelStore: ViewModelStore

    private var mFactory: ViewModelProvider.Factory? = null

    var onlineMode = false

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        if (bridge.crashHandler == Handle.XCRASH) {
            val callback = ICrashCallback { logPath, emergency ->
                val map = TombstoneParser.parse(logPath, emergency)
                val sb = StringBuilder()
                val mode = if (bridge.isDebug) "Debug" else "Release"
                sb.append("★ 项目名: ${AppUtils.getAppName()} (${mode}) ★\n")
                bridge.versionName?.run { sb.append("★ 版本号: ${bridge.versionName} ★\n") }
                bridge.serialNo?.run { sb.append("★ 设备号: ${bridge.serialNo} ★\n") }
                sb.append("★ 时间: ${DateUtils.getDateFormatString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")} ★\n")
                sb.append("★ 异常信息 ★\n")
                sb.append(map["java stacktrace"] ?: "")
                TombstoneManager.deleteTombstone(logPath)
                if (onlineMode) {
                    bridge.dingTalk?.run { sendDingTalk(sb.toString(), this) }
                    bridge.mail?.run { sendMail(sb.toString(), this) }
                }

                Thread.sleep(1000)
                restartApplication()
            }

            XCrash.init(
                this, XCrash.InitParameters()
                    .setJavaRethrow(false)
                    .setJavaDumpAllThreadsWhiteList(arrayOf("^main$", "^Binder:.*", ".*Finalizer.*"))
                    .setJavaDumpAllThreadsCountMax(10)
                    .setJavaCallback(callback)
                    .setNativeRethrow(false)
                    .setNativeLogCountMax(10)
                    .setNativeDumpAllThreadsWhiteList(
                        arrayOf(
                            "^xcrash\\.sample$",
                            "^Signal Catcher$",
                            "^Jit thread pool$",
                            ".*(R|r)ender.*",
                            ".*Chrome.*"
                        )
                    )
                    .setNativeDumpAllThreadsCountMax(10)
                    .setNativeCallback(callback)
                    .setAnrRethrow(false)
                    .setAnrLogCountMax(10)
                    .setAnrCallback(callback)
                    .setPlaceholderCountMax(3)
                    .setPlaceholderSizeKb(512)
                    .setLogDir(getExternalFilesDir("xcrash").toString())
                    .setLogFileMaintainDelayMs(1000)
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 校验 apk 文件
        OpensslUtil.verify(this)

        mAppViewModelStore = ViewModelStore()
        SpUtils.initMMKV(this)

        // 是否是上报模式
        onlineMode = getAppMetaData(this, "online_mode")

        //防止项目崩溃，崩溃后打开错误界面
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(bridge.crashHandler == Handle.CACO)//是否启用CustomActivityOnCrash崩溃拦截机制
            .showErrorDetails(false) //是否必须显示包含错误详细信息的按钮 default: true
            .showRestartButton(false) //是否必须显示“重新启动应用程序”按钮或“关闭应用程序”按钮default: true
            .logErrorOnRestart(false) //是否必须重新堆栈堆栈跟踪 default: true
            .trackActivities(true) //是否必须跟踪用户访问的活动及其生命周期调用 default: false
            .minTimeBetweenCrashesMs(2000) //应用程序崩溃之间必须经过的时间 default: 3000
            .restartActivity(bridge.restartActivity) // 重启的activity
            .errorActivity(ErrorActivity::class.java) //发生错误跳转的activity
            .apply()

        LogPrintUtils.setPrintLog(bridge.isDebug)
        AutoSizeConfig.getInstance().setLog(bridge.isDebug)
        Aria.init(this)
    }

    /**
     * 获取一个全局的ViewModel
     */
    fun getAppViewModelProvider(): ViewModelProvider {
        return ViewModelProvider(this, this.getAppFactory())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }

    private inline fun <reified T> getAppMetaData(
        context: Context,
        metaName: String
    ): T {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        when (T::class.java) {
            java.lang.Boolean::class.java -> {
                appInfo.metaData?.run {
                    for (key in appInfo.metaData.keySet()) {
                        if (metaName == key) {
                            return appInfo.metaData[key].toString().toBoolean() as T
                        }
                    }
                }
                return false as T
            }
            else -> {
                appInfo.metaData?.run {
                    for (key in appInfo.metaData.keySet()) {
                        if (metaName == key) {
                            return appInfo.metaData[key].toString() as T
                        }
                    }
                }
                return "" as T
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: BaseApp
    }
}