package com.zhushenwudi.base.mvvm.v

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.elvishew.xlog.XLog
import com.zhushenwudi.base.R
import com.zhushenwudi.base.app.BaseApp
import com.zhushenwudi.base.utils.DateUtils
import com.zhushenwudi.base.utils.SendUtil.sendDingTalk
import com.zhushenwudi.base.utils.SendUtil.sendMail
import dev.utils.app.AppUtils

class ErrorActivity : AppCompatActivity() {
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
                val sb = StringBuilder()
                val mode = if (isDebug) "Debug" else "Release"
                sb.append("★ 项目名: ${AppUtils.getAppName()} (${mode}) ★\n")
                versionName?.run { sb.append("★ 版本号: $versionName ★\n") }
                serialNo?.run { sb.append("★ 设备号: $serialNo ★\n") }
                sb.append(
                    "★ 时间: ${
                        DateUtils.getDateFormatString(
                            System.currentTimeMillis(),
                            "yyyy-MM-dd HH:mm:ss"
                        )
                    } ★\n"
                )
                sb.append("★ 异常信息 ★\n")
                sb.append(it)
                val message = sb.toString()
                XLog.e(message)
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