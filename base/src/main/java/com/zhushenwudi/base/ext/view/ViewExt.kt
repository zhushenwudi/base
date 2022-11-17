package com.zhushenwudi.base.ext.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.umeng.analytics.MobclickAgent
import com.zhushenwudi.base.R
import com.zhushenwudi.base.app.BaseApp
import com.zhushenwudi.base.app.appContext
import com.zhushenwudi.base.utils.SpUtils
import dev.utils.LogPrintUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

// 最近一次点击的时间
private var mLastClickTime: Long = 0

// 最近一次点击的控件ID
private var mLastClickViewId = 0

val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA) //设置日期格式

/**
 * 设置view显示
 */
fun View.visible() {
    visibility = View.VISIBLE
}


/**
 * 设置view占位隐藏
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrGone(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * 根据条件设置view显示隐藏 为true 显示，为false 隐藏
 */
fun View.visibleOrInvisible(flag: Boolean) {
    visibility = if (flag) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

/**
 * 设置view隐藏
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 将view转为bitmap
 */
@Deprecated("use View.drawToBitmap()")
fun View.toBitmap(scale: Float = 1f, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
    if (this is ImageView) {
        if (drawable is BitmapDrawable) return (drawable as BitmapDrawable).bitmap
    }
    this.clearFocus()
    val bitmap = createBitmapSafely(
        (width * scale).toInt(),
        (height * scale).toInt(),
        config,
        1
    )
    if (bitmap != null) {
        Canvas().run {
            setBitmap(bitmap)
            save()
            drawColor(Color.WHITE)
            scale(scale, scale)
            this@toBitmap.draw(this)
            restore()
            setBitmap(null)
        }
    }
    return bitmap
}

fun createBitmapSafely(width: Int, height: Int, config: Bitmap.Config, retryCount: Int): Bitmap? {
    try {
        return Bitmap.createBitmap(width, height, config)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
        if (retryCount > 0) {
            System.gc()
            return createBitmapSafely(width, height, config, retryCount - 1)
        }
        return null
    }
}


/**
 * @param interval 延时
 * @param withOthers 与其他控件同时判断快速点击，false计算自身控件的延时，true计算全部控件的延时
 * @param label 埋点名称
 * @param action 动作
 */
fun View.clickNoRepeat(
    interval: Long = 500,
    withOthers: Boolean = false,
    label: String = "",
    action: (view: View) -> Unit
) {
    setOnClickListener {
        if (!isFastDoubleClick(it, interval, withOthers)) {
            upReportTracePoint(label)
            action(it)
        }
    }
}

fun View.upReportTracePoint(label: String, fragmentName: String? = null) {
    if (label.isNotEmpty() && BaseApp.instance.traceList.isNotEmpty()) {
        val map = HashMap<String, Any>()
        val sb = StringBuilder()
        val info =
            BaseApp.instance.traceList.find {
                it.page == (fragmentName ?: findNavController().currentDestination?.label)
            }
        sb.append(info?.label ?: "-")
        sb.append(" - ")
        sb.append(label)
        LogPrintUtils.dTag("trace_point", sb.toString())
        map[if (BaseApp.instance.bridge.isDebug) "dev" else "prod"] = sb.toString()
        MobclickAgent.onEventObject(appContext, "event", map)
    }
}

fun BaseQuickAdapter<*, *>.clickNoRepeat(
    vararg label: Pair<Int, String>?,
    fragmentName: String?,
    action: (adapter: BaseQuickAdapter<*, *>?, view: View, position: Int) -> Unit
) {
    setOnItemClickListener { adapter, view, position ->
        view.upReportTracePoint(label[position]?.second ?: "", fragmentName)
        action(adapter, view, position)
    }
}

/**
 * 判断双击时间与 id
 * @param v 控件
 * @param intervalMillis 延时
 * @param withOthers 与其他控件同时判断快速点击
 */
private fun isFastDoubleClick(v: View, intervalMillis: Long, withOthers: Boolean = false): Boolean {
    val viewId = v.id
    val time = System.currentTimeMillis()
    val timeInterval = abs(time - mLastClickTime)
    return if ((withOthers && timeInterval < intervalMillis)
        || (!withOthers && timeInterval < intervalMillis && viewId == mLastClickViewId)
    )
        true
    else {
        mLastClickTime = time
        mLastClickViewId = viewId
        false
    }
}


fun Any?.notNull(notNullAction: (value: Any) -> Unit, nullAction1: () -> Unit) {
    if (this != null) {
        notNullAction.invoke(this)
    } else {
        nullAction1.invoke()
    }
}

// 绑定普通的Recyclerview
fun RecyclerView.init(
    layoutManger: RecyclerView.LayoutManager,
    bindAdapter: RecyclerView.Adapter<*>,
    isScroll: Boolean = true
): RecyclerView {
    layoutManager = layoutManger
    setHasFixedSize(true)
    adapter = bindAdapter
    isNestedScrollingEnabled = isScroll
    return this
}

// 设置适配器的列表动画
fun BaseQuickAdapter<*, *>.setAdapterAnimation(mode: Int) {
    //等于0，关闭列表动画 否则开启
    if (mode == 0) {
        this.animationEnable = false
    } else {
        this.animationEnable = true
        this.setAnimationWithDefault(BaseQuickAdapter.AnimationType.values()[mode - 1])
    }
}

/**
 * adapter加载更多
 *
 * @param list 列表<T>
 * @param pageNum 页数
 * @param pageSize 一页的数量
 * @param emptyListener 没有数据时执行的方法
 */
fun <T, BD : ViewDataBinding> BaseQuickAdapter<T, BaseDataBindingHolder<BD>>.loadMore(
    list: MutableList<T>?, pageNum: Int, pageSize: Int = 20, emptyListener: () -> Unit = {}
): Int {
    loadMoreModule.isEnableLoadMore = true
    if (pageNum == 1) {
        if (list == null || list.size == 0) {
            setList(null)
            emptyListener()
            return 1
        } else {
            setList(list)
        }

    } else {
        addData(list as MutableList<T>)
    }
    val page = pageNum + 1
    if (list.size < pageSize) {
        loadMoreModule.loadMoreEnd()
    } else {
        loadMoreModule.loadMoreComplete()
    }
    return page
}

fun ImageView.showRectPic(
    pic: String?,
    defPhoto: Int = R.drawable.icon_def,
    width: Int = 64,
    height: Int = 64
) {
    if (pic.isNullOrEmpty()) {
        load(defPhoto) {
            crossfade(false)
            size(width, height)
        }
    } else {
        load(pic) {
            crossfade(false)
            placeholder(defPhoto)
            error(defPhoto)
            size(width, height)
        }
    }
}

fun ImageView.showRoundPic(
    pic: String?,
    defPhoto: Int = R.drawable.icon_def,
    sideWidth: Int = 64
) {
    if (pic.isNullOrEmpty()) {
        load(defPhoto) {
            crossfade(false)
            size(sideWidth, sideWidth)
            transformations(CircleCropTransformation())
        }
    } else {
        load(pic) {
            crossfade(false)
            placeholder(defPhoto)
            error(defPhoto)
            size(sideWidth, sideWidth)
            transformations(CircleCropTransformation())
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun View.resource(resource: Int) = context.getColor(resource)

@RequiresApi(Build.VERSION_CODES.M)
fun Context.resource(resource: Int) = getColor(resource)

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.resource(resource: Int) = getColor(resource)

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.resource(resource: Int) = this.requireContext().getColor(resource)