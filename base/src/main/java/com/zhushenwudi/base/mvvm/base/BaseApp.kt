package com.zhushenwudi.base.mvvm.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.alley.openssl.OpensslUtil
import com.tencent.bugly.Bugly
import com.tencent.bugly.crashreport.CrashReport
import com.zhushenwudi.base.mvvm.util.SpUtils
import com.zhushenwudi.base.mvvm.util.getDeviceSN
import com.zhushenwudi.base.mvvm.util.isApkInDebug
import com.zhushenwudi.base.mvvm.util.restartApplication

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/14
 * 描述　: 对于写BaseApp，其实我是拒绝的，但是需要提供一个很有用的功能--在Activity/fragment中获取Application级别的ViewModel
 * 所以才硬着头皮加的，如果你不想继承BaseApp，又想获取Application级别的ViewModel功能
 * 那么你可以复制该类的代码到你的自定义Application中去，然后可以自己写获取ViewModel的拓展函数即 :
 * GetViewModelExt类的getAppViewModel方法
 */

open class BaseApp : Application(), ViewModelStoreOwner {

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
                if (!isApkInDebug(this@BaseApp)) {
                    restartApplication()
                }
                return null
            }
        })

        Bugly.init(this, getAppMetaData(this), false, strategy)
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

    private fun getAppMetaData(
        context: Context,
        metaName: String = "bugly_id"
    ): String {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )

        appInfo.metaData?.run {
            for (key in appInfo.metaData.keySet()) {
                if (metaName == key) {
                    return appInfo.metaData[key].toString()
                }
            }
        }
        return ""
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}