package com.zhushenwudi.base.mvvm.m

data class PageList<T>(
    val current: Int,
    val pages: Int,
    val records: MutableList<T>?,
    val total: Int,
    val size: Int
)
