package com.zhushenwudi.base.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.alley.openssl.OpensslUtil
import com.tencent.bugly.crashreport.CrashReport
import com.zhushenwudi.base.mvvm.v.ErrorActivity
import com.zhushenwudi.base.utils.*

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/14
 * 描述　: 对于写BaseApp，其实我是拒绝的，但是需要提供一个很有用的功能--在Activity/fragment中获取Application级别的ViewModel
 * 所以才硬着头皮加的，如果你不想继承BaseApp，又想获取Application级别的ViewModel功能
 * 那么你可以复制该类的代码到你的自定义Application中去，然后可以自己写获取ViewModel的拓展函数即 :
 * GetViewModelExt类的getAppViewModel方法
 */

open class BaseApp(private val isDebug: Boolean, private val mailVerify: String) : Application(), ViewModelStoreOwner {

    private lateinit var mAppViewModelStore: ViewModelStore

    private var mFactory: ViewModelProvider.Factory? = null

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }

    override fun onCreate() {
        super.onCreate()
        // 校验 apk 文件
        OpensslUtil.verify(this)

        mAppViewModelStore = ViewModelStore()
        SpUtils.initMMKV(this)

        // 初始化Bugly
        val strategy = CrashReport.UserStrategy(this)
        strategy.deviceID = getDeviceSN()
        strategy.setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
            override fun onCrashHandleStart(
                crashType: Int,
                errorType: String,
                errorMessage: String,
                errorStack: String
            ): Map<String, String>? {
                if (crashType < 2) {
                    try {
                        val intent = Intent(this@BaseApp, ErrorActivity::class.java)
                        intent.putExtra(ErrorActivity.ERROR_TYPE, errorType)
                        intent.putExtra(ErrorActivity.ERROR_MESSAGE, errorMessage)
                        intent.putExtra(ErrorActivity.ERROR_STACK, errorStack)
                        intent.putExtra(ErrorActivity.IS_DEBUG, isDebug)
                        intent.putExtra(ErrorActivity.MAIL_VERIFY, mailVerify)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (!isDebug) {
                        restartApplication()
                    }
                }
                return null
            }
        })

        // 是否是上报模式
        val onlineMode = getAppMetaData<Boolean>(this, "online_mode")
        val buglyId = getAppMetaData<String>(this, "bugly_id")

        CrashReport.initCrashReport(
            this,
            if (onlineMode) buglyId else "",
            isDebug,
            strategy
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

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}