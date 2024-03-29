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
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.ConsolePrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import com.jeremyliao.liveeventbus.LiveEventBus
import com.umeng.commonsdk.UMConfigure
import com.zhushenwudi.base.mvvm.m.Bridge
import com.zhushenwudi.base.mvvm.m.Handle
import com.zhushenwudi.base.mvvm.m.TracePageInfo
import com.zhushenwudi.base.mvvm.v.ErrorActivity
import com.zhushenwudi.base.utils.DateUtils
import com.zhushenwudi.base.utils.SendUtil.sendDingTalk
import com.zhushenwudi.base.utils.SendUtil.sendMail
import com.zhushenwudi.base.utils.SpUtils
import com.zhushenwudi.base.utils.fromJson
import com.zhushenwudi.base.utils.restartApplication
import dev.utils.LogPrintUtils
import dev.utils.app.AppUtils
import dev.utils.app.PathUtils
import dev.utils.common.CloseUtils
import dev.utils.common.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.jessyan.autosize.AutoSizeConfig
import xcrash.ICrashCallback
import xcrash.TombstoneManager
import xcrash.TombstoneParser
import xcrash.XCrash
import java.io.File
import java.io.InputStream

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

    val traceList = mutableListOf<TracePageInfo>()

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
                sb.append(
                    "★ 时间: ${
                        DateUtils.getDateFormatString(
                            System.currentTimeMillis(),
                            "yyyy-MM-dd HH:mm:ss"
                        )
                    } ★\n"
                )
                sb.append("★ 异常信息 ★\n")
                sb.append(map["java stacktrace"] ?: "")
                TombstoneManager.deleteTombstone(logPath)
                if (onlineMode) {
                    bridge.dingTalk?.run { sendDingTalk(sb.toString(), this) }
                    bridge.mail?.run { sendMail(sb.toString(), this) }
                }
                XLog.e(sb.toString())
                Thread.sleep(1000)
                restartApplication()
            }

            XCrash.init(
                this, XCrash.InitParameters()
                    .setJavaRethrow(false)
                    .setJavaDumpAllThreadsWhiteList(
                        arrayOf(
                            "^main$",
                            "^Binder:.*",
                            ".*Finalizer.*"
                        )
                    )
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

        if (bridge.needVerifySignature) {
            // 校验 apk 文件
            OpensslUtil.verify(this)
        }

        mAppViewModelStore = ViewModelStore()
        SpUtils.initMMKV(this)

        // 是否是上报模式
        onlineMode = getAppMetaData(this, "online_mode")

        // 埋点页面解析
        initTracePointData()

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
        LiveEventBus.config().enableLogger(bridge.isDebug)
        Aria.init(this)
        initLogger()

        if (bridge.uMeng == null) {
            return
        }
        MainScope().launch(Dispatchers.IO) {
            UMConfigure.setLogEnabled(true)
            val env = if (bridge.isDebug) "PRE" else "PROD"
            UMConfigure.preInit(this@BaseApp, bridge.uMeng, env)
            UMConfigure.submitPolicyGrantResult(this@BaseApp, true)
            UMConfigure.init(this@BaseApp, bridge.uMeng, env, UMConfigure.DEVICE_TYPE_BOX, "")
        }
    }

    private fun initLogger() {
        val config = LogConfiguration.Builder()
            .tag("iLab")
            .enableThreadInfo() // 允许打印线程信息，默认禁止
            .enableStackTrace(2) // 允许打印深度为 2 的调用栈信息，默认禁止
            .enableBorder() // 允许打印日志边框，默认禁止
            .build()

        val androidPrinter = AndroidPrinter(true) // 通过 android.util.Log 打印日志的打印器

        val consolePrinter = ConsolePrinter() // 通过 System.out 打印日志到控制台的打印器

        val logDir = PathUtils.getAppExternal().appDataPath + File.separator + "log"
        FileUtils.createOrExistsDir(logDir)

        val filePrinter = FilePrinter.Builder(logDir) // 指定保存日志文件的路径
            .fileNameGenerator(DateFileNameGenerator()) // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
            .backupStrategy(
                FileSizeBackupStrategy2(
                    1024 * 1024,
                    FileSizeBackupStrategy2.NO_LIMIT
                )
            ) // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
            .cleanStrategy(FileLastModifiedCleanStrategy(2626560000)) // 指定日志文件清除策略，默认为 NeverCleanStrategy()
            .build()

        XLog.init( // 初始化 XLog
            config,  // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
            androidPrinter,  // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter(Android)/ConsolePrinter(java)
            consolePrinter,
            filePrinter
        )
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

    open fun initTracePointData() {
        val fileName = "page_trace_info.json"
        val bytes = readFromAsset(fileName) ?: return
        val json = String(bytes, Charsets.UTF_8)
        if (json.isEmpty()) {
            return
        }
        fromJson<MutableList<TracePageInfo>>(json)?.let { traceList.addAll(it) }
    }

    private fun readFromAsset(fileName: String): ByteArray? {
        var ins: InputStream? = null
        try {
            ins = resources.assets.open(fileName)
            val length = ins.available()
            val buffer = ByteArray(length)
            ins.read(buffer)
            return buffer
        } catch (e: Exception) {
            LogPrintUtils.e(e)
        } finally {
            ins?.let { CloseUtils.closeIOQuietly(it) }
        }
        return null
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: BaseApp
    }
}