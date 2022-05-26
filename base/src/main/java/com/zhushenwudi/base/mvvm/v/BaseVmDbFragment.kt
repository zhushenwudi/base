package com.zhushenwudi.base.mvvm.v

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.zhushenwudi.base.ext.inflateBinding
import com.zhushenwudi.base.mvvm.vm.BaseAppViewModel

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/12
 * 描述　: ViewModelFragment基类，自动把ViewModel注入Fragment和Databind注入进来了
 * 需要使用Databind的清继承它
 */
abstract class BaseVmDbFragment<VM : BaseAppViewModel, VB : ViewDataBinding> :
    BaseVmFragment<VM>() {

    //该类绑定的ViewDataBinding
    private var _binding: VB? = null
    val bind get() = _binding!!

    override fun initViewDataBind(inflater: LayoutInflater, container: ViewGroup?): View? {
        _binding = inflateBinding(inflater, container, false)
        return bind.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.unbind()
        _binding = null
    }
}