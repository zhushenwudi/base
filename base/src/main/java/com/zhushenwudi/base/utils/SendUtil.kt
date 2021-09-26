package com.zhushenwudi.base.utils

import com.lzy.okgo.OkGo
import com.teprinciple.mailsender.Mail
import com.teprinciple.mailsender.MailSender
import com.zhushenwudi.base.mvvm.m.*
import dev.utils.app.AppUtils
import kotlinx.coroutines.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SendUtil {
    private const val SHA256 = "HmacSHA256"
    private const val DING_TALK_URL = "https://oapi.dingtalk.com/robot/send"
    private const val HEADER_KEY = "Content-Type"
    private const val HEADER_VALUE = "application/json; charset=utf-8"
    private const val ENCODER = "UTF-8"

    private const val SERVER_HOST = "smtp.qq.com"
    private const val SERVER_PORT = "587"
    private const val CRASH_TITLE = " Crash Found!!"

    fun sendMail(message: String, mailBean: MailBean) {
        try {
            val mail = Mail().apply {
                mailServerHost = SERVER_HOST
                mailServerPort = SERVER_PORT
                fromAddress = mailBean.from
                password = mailBean.mailVerify
                toAddress = mailBean.to
                subject = AppUtils.getAppName() + CRASH_TITLE
                content = message
//                attachFiles = arrayListOf(file)
            }
            MailSender.getInstance().sendMail(mail)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendDingTalk(content: String, dingTalkBean: DingTalkBean) {
        try {
            val dingTalk = DingTalk(TextBean(content), AtBean(dingTalkBean.to))
            val timestamp = System.currentTimeMillis()
            val strToSign = "${timestamp}\n${dingTalkBean.secret}"
            val mac = Mac.getInstance(SHA256)
            mac.init(SecretKeySpec(dingTalkBean.secret.toByteArray(StandardCharsets.UTF_8), SHA256))
            val signData = mac.doFinal(strToSign.toByteArray(StandardCharsets.UTF_8))
            val sign = URLEncoder.encode(EncodeUtil().encode(signData), ENCODER)
            val url = "${DING_TALK_URL}?access_token=${dingTalkBean.token}&sign=${sign}&timestamp=${timestamp}"
            MainScope().launch(Dispatchers.IO) {
                OkGo.post<String>(url)
                    .tag(this)
                    .headers(HEADER_KEY, HEADER_VALUE)
                    .upJson(toJson(dingTalk))
                    .execute()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}