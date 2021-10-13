package com.zhushenwudi.base.mvvm.m

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Bridge(
    var isDebug: Boolean,
    var versionName: String? = null,
    var serialNo: String? = null,
    var mail: MailBean? = null,
    var dingTalk: DingTalkBean? = null
) : Parcelable

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
