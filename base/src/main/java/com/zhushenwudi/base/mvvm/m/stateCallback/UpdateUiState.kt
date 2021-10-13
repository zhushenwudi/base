package com.zhushenwudi.base.mvvm.m.stateCallback

import androidx.annotation.Keep

/**
 * 作者　: hegaojian
 * 时间　: 2020/3/11
 * 描述　:操作数据的状态类
 */
@Keep
data class UpdateUiState<T>(
    //请求是否成功
    var isSuccess: Boolean = true,
    //操作的对象
    var data: T? = null,
    //请求失败的错误信息
    var errorMsg: String = ""
)