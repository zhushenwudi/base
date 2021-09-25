package com.zhushenwudi.base.mvvm.v

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zhushenwudi.base.R
import com.teprinciple.mailsender.Mail
import com.teprinciple.mailsender.MailSender
import com.zhushenwudi.base.utils.restartApplication
import dev.utils.app.AppUtils
import java.util.*

class ErrorActivity: AppCompatActivity() {
    private val mailArr = arrayListOf(TO_MAIL)
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
            val isDebug = getBooleanExtra(IS_DEBUG, true)
            val mailVerify = getStringExtra(MAIL_VERIFY) ?: ""
            Log.e("aaa", "$errorType")
            Log.e("aaa", "$errorMessage")
            Log.e("aaa", "$errorStack")

            if (isDebug) {
                tvError.text = errorType
                tvMessage.text = errorMessage
                tvStack.text = errorStack
                linearLayout.visibility = View.VISIBLE
            } else {
                tvTitle.visibility = View.VISIBLE
                tvHint.visibility = View.VISIBLE
            }

            val sj = StringJoiner("\n")
            sj.add(errorType)
            sj.add(errorMessage)
            sj.add(errorStack)
            sendMail(sj.toString(), mailVerify)
        }
    }

    private fun sendMail(message: String, mailVerify: String) {
        try {
            val mail = Mail().apply {
                mailServerHost = SERVER_HOST
                mailServerPort = SERVER_PORT
                fromAddress = FROM_MAIL
                password = mailVerify
                toAddress = mailArr
                subject = AppUtils.getAppName() + CRASH_TITLE
                content = message
//                attachFiles = arrayListOf(file)
            }
            MailSender.getInstance().sendMail(mail)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun btnReset(v: View) {
        restartApplication()
    }

    companion object {
        const val ERROR_TYPE = "errorType"
        const val ERROR_MESSAGE = "errorMessage"
        const val ERROR_STACK = "errorStack"
        const val IS_DEBUG = "isDebug"
        const val SERVER_HOST = "smtp.qq.com"
        const val SERVER_PORT = "587"
        const val FROM_MAIL = "404288461@qq.com"
        const val MAIL_VERIFY = "mailVerify"
        const val TO_MAIL = "gr.zhu@ilabservice.com"
        const val CRASH_TITLE = " Crash Found!!"
    }
}