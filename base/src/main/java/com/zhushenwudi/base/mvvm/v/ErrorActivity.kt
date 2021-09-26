package com.zhushenwudi.base.mvvm.v

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zhushenwudi.base.R
import com.zhushenwudi.base.mvvm.m.Bridge
import com.zhushenwudi.base.utils.SendUtil.sendDingTalk
import com.zhushenwudi.base.utils.SendUtil.sendMail
import com.zhushenwudi.base.utils.restartApplication
import java.util.*

class ErrorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val tvError = findViewById<TextView>(R.id.tvError)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvStack = findViewById<TextView>(R.id.tvStack)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvHint = findViewById<TextView>(R.id.tvHint)
        tvStack.movementMethod = ScrollingMovementMethod()

        intent?.run {
            val errorType = getStringExtra(ERROR_TYPE)
            val errorMessage = getStringExtra(ERROR_MESSAGE)
            val errorStack = getStringExtra(ERROR_STACK)
            val bridge = getParcelableExtra<Bridge>(BRIDGE)
            val isOnline = getBooleanExtra(IS_ONLINE, false)
            Log.e("aaa", "$errorType")
            Log.e("aaa", "$errorMessage")
            Log.e("aaa", "$errorStack")

            bridge?.run {
                if (isDebug) {
                    tvError.text = errorType
                    tvMessage.text = errorMessage
                    tvStack.text = errorStack
                    linearLayout.visibility = View.VISIBLE
                } else {
                    tvTitle.visibility = View.VISIBLE
                    tvHint.visibility = View.VISIBLE
                }

                // 只有非本地部署环境才会发送
                if (isOnline) {
                    val sj = StringJoiner("\n")
                    sj.add(errorType)
                    sj.add(errorMessage)
                    sj.add(errorStack)
                    val content = sj.toString()

                    bridge.mail?.run { sendMail(content, this) }
                    bridge.dingTalk?.run { sendDingTalk(content, this) }
                }
            }
        }
    }



    fun btnReset(v: View) {
        restartApplication()
    }

    companion object {
        const val ERROR_TYPE = "errorType"
        const val ERROR_MESSAGE = "errorMessage"
        const val ERROR_STACK = "errorStack"
        const val IS_ONLINE = "isOnline"
        const val BRIDGE = "bridge"
    }
}