package com.zhushenwudi.base.mvvm.v

import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.zhushenwudi.base.R
import com.zhushenwudi.base.app.BaseApp
import com.zhushenwudi.base.utils.DateUtils
import com.zhushenwudi.base.utils.SendUtil.sendDingTalk
import com.zhushenwudi.base.utils.SendUtil.sendMail
import dev.utils.app.AppUtils
import io.sentry.Sentry
import java.util.*

class ErrorActivity: AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val tvStack = findViewById<TextView>(R.id.tvStack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvHint = findViewById<TextView>(R.id.tvHint)
        tvStack.movementMethod = ScrollingMovementMethod()

        CustomActivityOnCrash.getStackTraceFromIntent(intent)?.let {
            BaseApp.instance.bridge.run {
                if (isDebug) {
                    tvStack.text = it
                    linearLayout.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.VISIBLE
                    tvHint.visibility = View.VISIBLE
                }
            }
        }

        CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, intent).let {
            BaseApp.instance.bridge.run {
                val sj = StringJoiner("\n")
                val mode = if (isDebug) "Debug" else "Release"
                sj.add("★ 项目名: ${AppUtils.getAppName()} (${mode}) ★")
                versionName?.run { sj.add("★ 版本号: $versionName ★") }
                serialNo?.run { sj.add("★ 设备号: $serialNo ★") }
                sj.add("★ 时间: ${DateUtils.getDateFormatString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")} ★")
                sj.add("★ 异常信息 ★")
                sj.add(it)
                val message = sj.toString()
                if (BaseApp.instance.onlineMode) {
                    mail?.run { sendMail(message, this) }
                    dingTalk?.run { sendDingTalk(message, this) }
                }
            }
        }
    }

    fun btnReset(v: View) {
        CustomActivityOnCrash.getConfigFromIntent(intent)?.run {
            CustomActivityOnCrash.restartApplication(this@ErrorActivity, this)
        }
    }
}