package com.zhushenwudi.base.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent.*
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.utils.app.AppUtils
import dev.utils.app.PathUtils
import dev.utils.app.permission.PermissionUtils
import dev.utils.common.FileUtils
import dev.utils.common.ZipUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.math.BigDecimal
import java.util.regex.Pattern
import kotlin.system.exitProcess

private var mLastClick: Long = 0
private const val TIMER = 3
private var quickCounts = LongArray(15)
val gson = Gson()

// json 字符串 -> 对象
inline fun <reified T> fromJson(json: String): T? {
    return try {
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(json, type)
    } catch (e: Exception) {
        null
    }
}

// 对象 -> json 字符串
inline fun <reified T> toJson(obj: T): String {
    return gson.toJson(obj)
}

// 倒计时 3 秒
fun countDownTimer(
    total: Int = TIMER,
    onTick: (Int) -> Unit = {},
    onFinish: () -> Unit,
    scope: CoroutineScope
): Job {
    return flow {
        for (i in total downTo 0) {
            emit(i)
            delay(1000)
        }
    }
        .flowOn(Dispatchers.Default)
        .onCompletion { cause ->
            if (cause == null) {
                onFinish.invoke()
            }
        }
        .onEach { onTick.invoke(it) }
        .flowOn(Dispatchers.Main)
        .launchIn(scope)
}

// 输入搜索
fun <T, R> searchFilter(
    stateFlow: MutableStateFlow<T>,
    filter: (T) -> Boolean,
    flatMap: (T) -> Flow<R>,
    onResult: (R) -> Unit,
    scope: CoroutineScope
) {
    stateFlow
        .debounce(400)
        .filter { filter.invoke(it) }
        .distinctUntilChanged()
        .flatMapLatest { flatMap.invoke(it) }
        .catch { Log.e("aaa", "${it.message}") }
        .flowOn(Dispatchers.Default)
        .onEach { onResult.invoke(it) }
        .flowOn(Dispatchers.Main)
        .launchIn(scope)
}

// 数组深拷贝
fun Array<*>.copy(): Array<*> {
    return this.copyOf()
}

// 字符串深拷贝
fun String.copy(): String {
    return this + ""
}

// 获取 TAG
fun Any.TAG(): String {
    return this::class.java.simpleName
}

/**
 * 使用 Flow 做的简单的轮询
 * 请使用单独的协程来进行管理该 Flow
 *
 * @param intervals 轮询间隔时间(ms)
 * @param delay 轮训前的延迟时间(ms)
 * @param block 需要执行的代码块
 */
suspend fun startPolling(intervals: Long, delay: Long = 0, block: () -> Unit) {
    flow {
        delay(delay)
        while (true) {
            emit(0)
            delay(intervals)
        }
    }
        .catch { Log.e("flow", "startPolling: $it") }
        .flowOn(Dispatchers.Default)
        .collect { block.invoke() }
}

// 函数节流
fun funcThrottle(milliSeconds: Long = 500): Boolean {
    if (System.currentTimeMillis() - mLastClick <= milliSeconds) {
        return true
    }
    mLastClick = System.currentTimeMillis()
    return false
}

@SuppressLint("HardwareIds")
fun getDeviceSN(): String? {
    return Build.SERIAL
}

// 匹配是否为数字
fun isNumeric(str: String): Boolean {
    // 该正则表达式可以匹配所有的数字 包括负数
    val pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?")
    val bigStr = try {
        BigDecimal(str).toString()
    } catch (e: java.lang.Exception) {
        return false //异常 说明包含非数字。
    }
    val isNum = pattern.matcher(bigStr) // matcher是全匹配
    return isNum.matches()
}

// ascii 转 字符串
fun asciiToString(value: String): String {
    val sbu = StringBuilder()
    val chars = value.split(",").toTypedArray()
    for (aChar in chars) {
        sbu.append(aChar.toInt().toChar())
    }
    return sbu.toString()
}

// 重启应用程序
fun restartApplication() {
    val intent = AppUtils.getPackageManager().getLaunchIntentForPackage(AppUtils.getPackageName())
    intent?.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_CLEAR_TASK)
    AppUtils.startActivity(intent)
    Process.killProcess(Process.myPid())
    exitProcess(0)
}

// 预留后门
fun quickExit() {
    System.arraycopy(quickCounts, 1, quickCounts, 0, quickCounts.size - 1)
    quickCounts[quickCounts.size - 1] = SystemClock.uptimeMillis()
    if (quickCounts[0] > SystemClock.uptimeMillis() - 5000) {
        exitProcess(0)
    }
}

// 压缩离线日志
suspend fun zipLog(): Boolean {
    if (PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        val logDir =
            PathUtils.getAppExternal().appDataPath + File.separator + "log" + File.separator
        val outputDir =
            PathUtils.getSDCard().sdCardPath + File.separator + "smartlab" + File.separator
        if (FileUtils.createOrExistsDir(outputDir)) {
            return ZipUtils.zipFiles(FileUtils.listFilesInDir(logDir), File(outputDir + "log.zip"))
        }
    }
    return false
}