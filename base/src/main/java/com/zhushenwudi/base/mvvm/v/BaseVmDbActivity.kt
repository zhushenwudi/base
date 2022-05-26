package com.zhushenwudi.base.mvvm.v

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.noober.background.BackgroundLibrary
import com.zhushenwudi.base.ext.inflateBinding
import com.zhushenwudi.base.mvvm.vm.BaseAppViewModel

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/12
 * 描述　: 包含ViewModel 和Databind ViewModelActivity基类，把ViewModel 和Databind注入进来了
 * 需要使用Databind的清继承它
 */
abstract class BaseVmDbActivity<VM : BaseAppViewModel, VB : ViewDataBinding> :
    BaseVmActivity<VM>() {

    lateinit var bind: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        BackgroundLibrary.inject2(this)
        super.onCreate(savedInstanceState)
    }

    /**
     * 创建DataBinding
     */
    override fun initDataBind(): View {
        bind = inflateBinding()
        return bind.root
    }
}