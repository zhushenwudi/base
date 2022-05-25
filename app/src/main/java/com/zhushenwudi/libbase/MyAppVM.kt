package com.zhushenwudi.libbase

import com.zhushenwudi.base.livedata.event.EventLiveData
import com.zhushenwudi.base.mvvm.vm.BaseAppViewModel

class MyAppVM : BaseAppViewModel() {
    // 网络请求错误信息
    val requestFailed = EventLiveData<String>()
}