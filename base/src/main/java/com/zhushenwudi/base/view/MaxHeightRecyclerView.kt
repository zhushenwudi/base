package com.zhushenwudi.base.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.zhushenwudi.base.R

class MaxHeightRecyclerView : RecyclerView {
    private var mMaxHeight = 0
    private var mWidth = 0

    constructor(context: Context?) : super(context!!)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initialize(context, attrs)
    }

    /**
     * 设置最大高度
     *
     * @param maxHeight 最大高度 px
     */
    fun setMaxHeight(maxHeight: Int) {
        mMaxHeight = maxHeight
        // 重绘 RecyclerView
        requestLayout()
    }

    fun setWidth(width: Int) {
        mWidth = width
        requestLayout()
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        val arr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView)
        mMaxHeight = arr.getLayoutDimension(R.styleable.MaxHeightRecyclerView_maxHeight, mMaxHeight)
        arr.recycle()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var mWidthSpec = widthSpec
        var mHeightSpec = heightSpec
        if (mMaxHeight > 0) {
            mHeightSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST)
        }
        if (mWidth > 0) {
            mWidthSpec = MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY)
        }
        super.onMeasure(mWidthSpec, mHeightSpec)
    }
}