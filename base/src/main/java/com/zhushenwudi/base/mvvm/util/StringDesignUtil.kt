package com.zhushenwudi.base.mvvm.util

import android.text.*
import android.text.style.ForegroundColorSpan

object StringDesignUtil {

    /**
     * 方法描述：根据文本下标，指定单个部分文字变色
     **/
    fun getSpannableStringBuilder(
        text: String,
        color: Int,
        startIndex: Int,
        entIndex: Int
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        if (startIndex in 0 until entIndex && entIndex <= text.length) {
            builder.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                entIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return builder
    }

    /**
     * 方法描述：指定关键字变色，并且给相对应的关键指定颜色
     */
    fun getSpannableStringBuilder(
        text: String,
        texts: Array<String>?,
        color: IntArray?
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(text)
        if (texts != null) {
            for (i in texts.indices) {
                val value = texts[i]
                if (!TextUtils.isEmpty(value) && text.contains(value)) {
                    if (color != null && color.size > i) {
                        val startIndex = text.indexOf(value)
                        val entIndex = startIndex + value.length
                        if (entIndex > startIndex && entIndex <= text.length) {
                            builder.setSpan(
                                ForegroundColorSpan(color[i]),
                                startIndex,
                                entIndex,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
            }
        }
        return builder
    }

    /**
     * 方法描述： 文本由关键字拼接而成，文本内容与字体颜色一一对应显示
     */
    fun getSpannableStringBuilder(
        text: Array<String?>?,
        color: IntArray?
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        if (text != null) {
            for (i in text.indices) {
                val startIndex = builder.length
                builder.append(text[i])
                if (color != null && color.size > i) {
                    builder.setSpan(
                        ForegroundColorSpan(color[i]),
                        startIndex,
                        builder.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return builder
    }

    /**
     * 根据关键字，指定单个部分文字颜色
     */
    fun getSpanned(text: String, keyword: String, colorValue: String): Spanned? {
        return Html.fromHtml(text.replace(keyword, "<font color=$colorValue>$keyword</font>"))
    }

    /**
     * 指定关键字变色，并且给相对应的关键指定颜色
     */
    fun getSpanned(text: String, keyword: Array<String>, colorValue: Array<String>): Spanned? {
        var mText = text
        for (i in keyword.indices) {
            if (colorValue.size > i) {
                mText = mText.replace(
                    keyword[i],
                    "<font color=" + colorValue[i] + ">" + keyword[i] + "</font>"
                )
            }
        }
        return Html.fromHtml(mText)
    }

    /**
     * 文本由关键字拼接而成，文本内容与字体颜色一一对应显示
     */
    fun getSpanned(keyword: Array<String>, colorValue: Array<String>): Spanned {
        val buffer = StringBuilder()
        for (i in keyword.indices) {
            if (colorValue.size > i) {
                buffer.append("<font color=").append(colorValue[i]).append(">")
                    .append(keyword[i]).append("</font>")
            }
        }
        return Html.fromHtml(buffer.toString())
    }
}