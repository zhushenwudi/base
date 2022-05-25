package com.zhushenwudi.libbase

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.zhushenwudi.base.ext.view.clickNoRepeat
import com.zhushenwudi.base.livedata.event.EventLiveData
import com.zhushenwudi.base.login.LoginUtil
import com.zhushenwudi.base.mvvm.v.BaseVmDbActivity
import com.zhushenwudi.base.utils.MqttManager
import com.zhushenwudi.base.utils.zipLog
import com.zhushenwudi.libbase.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity: BaseVmDbActivity<MainViewModel, ActivityMainBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        findViewById<Button>(R.id.button).clickNoRepeat {
            1/0
        }
    }

    override fun showLoading(message: String) {
    }

    override fun dismissLoading() {
    }

    override fun createObserver() {

    }
}