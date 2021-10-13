package com.zhushenwudi.base.mvvm.m

import androidx.annotation.Keep

@Keep
data class DingTalk(
    var text: TextBean,
    var at: AtBean,
    var msgtype: String = "text"
)

@Keep
data class TextBean(
    var content: String
)

@Keep
data class AtBean(
    var atMobiles: ArrayList<String>,
    var isAtAll: Boolean = false
)
