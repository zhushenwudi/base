package com.zhushenwudi.base.utils

import android.content.Context
import com.getkeepsafe.relinker.ReLinker
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel

/**
 * MMKV使用封装
 *
 * @author Qu Yunshuo
 * @since 8/28/20
 */
object SpUtils {

    /**
     * 初始化
     */
    fun initMMKV(context: Context) {
        MMKV.initialize(
            context,
            { libName -> ReLinker.loadLibrary(context, libName) },
            MMKVLogLevel.LevelInfo
        )
    }

    /**
     * 保存数据（简化）
     * 根据value类型自动匹配需要执行的方法
     */
    fun put(key: String, value: Any) =
        when (value) {
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Double -> putDouble(key, value)
            is String -> putString(key, value)
            is Boolean -> putBoolean(key, value)
            else -> false
        }

    fun putString(key: String, value: String): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getString(key: String, defValue: String = ""): String =
        MMKV.defaultMMKV()?.decodeString(key, defValue) ?: defValue

    fun putInt(key: String, value: Int): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getInt(key: String, defValue: Int = 0): Int = MMKV.defaultMMKV()?.decodeInt(key, defValue) ?: defValue

    fun putLong(key: String, value: Long): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getLong(key: String, defValue: Long = 0L): Long = MMKV.defaultMMKV()?.decodeLong(key, defValue) ?: defValue

    fun putDouble(key: String, value: Double): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getDouble(key: String, defValue: Double = 0.0): Double =
        MMKV.defaultMMKV()?.decodeDouble(key, defValue) ?: defValue

    fun putFloat(key: String, value: Float): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getFloat(key: String, defValue: Float = 0F): Float =
        MMKV.defaultMMKV()?.decodeFloat(key, defValue) ?: defValue

    fun putBoolean(key: String, value: Boolean): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

    fun getBoolean(key: String, defValue: Boolean = false): Boolean =
        MMKV.defaultMMKV()?.decodeBool(key, defValue) ?: defValue

    fun remove(key: String) {
        MMKV.defaultMMKV()?.removeValueForKey(key)
    }

    fun removeAll(keys: Array<String>) {
        MMKV.defaultMMKV()?.removeValuesForKeys(keys)
    }

    fun contains(key: String): Boolean? = MMKV.defaultMMKV()?.contains(key)
}