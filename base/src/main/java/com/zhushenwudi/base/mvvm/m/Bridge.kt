package com.zhushenwudi.base.mvvm.m

import android.app.Activity
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Bridge(
    var isDebug: Boolean,
    var crashHandler: Handle = Handle.XCRASH,
    var restartActivity: Class<out Activity>,
    var versionName: String? = null,
    var serialNo: String? = null,
    var mail: MailBean? = null,
    var dingTalk: DingTalkBean? = null,
    var uMeng: String? = null,
    var needVerifySignature: Boolean = true
) : Parcelable

@Keep
enum class Handle {
    NO,
    CACO,
    XCRASH
}

@Keep
@Parcelize
data class MailBean(
    var from: String,
    var to: ArrayList<String>,
    var mailVerify: String
) : Parcelable

@Keep
@Parcelize
data class DingTalkBean(
    var token: String,
    var secret: String,
    var to: ArrayList<String>
) : Parcelable
