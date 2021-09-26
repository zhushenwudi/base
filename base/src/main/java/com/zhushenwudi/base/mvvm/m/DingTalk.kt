package com.zhushenwudi.base.mvvm.m

data class DingTalk(
    var text: TextBean,
    var at: AtBean,
    var msgtype: String = "text"
)

data class TextBean(
    var content: String
)

data class AtBean(
    var atMobiles: ArrayList<String>,
    var isAtAll: Boolean = false
)
