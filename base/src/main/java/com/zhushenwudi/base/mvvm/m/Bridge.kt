package com.zhushenwudi.base.mvvm.m

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bridge(
    var isDebug: Boolean,
    var mail: MailBean? = null,
    var dingTalk: DingTalkBean? = null
) : Parcelable

@Parcelize
data class MailBean(
    var from: String,
    var to: ArrayList<String>,
    var mailVerify: String
) : Parcelable

@Parcelize
data class DingTalkBean(
    var token: String,
    var secret: String,
    var to: ArrayList<String>
) : Parcelable
