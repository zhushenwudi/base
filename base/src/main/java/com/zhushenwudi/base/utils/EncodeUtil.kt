package com.zhushenwudi.base.utils

import android.text.TextUtils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class EncodeUtil {
    private val last2byte = "00000011".toInt(2).toChar()
    private val last4byte = "00001111".toInt(2).toChar()
    private val last6byte = "00111111".toInt(2).toChar()
    private val lead6byte = "11111100".toInt(2).toChar()
    private val lead4byte = "11110000".toInt(2).toChar()
    private val lead2byte = "11000000".toInt(2).toChar()
    private val encodeTable = charArrayOf(
        'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
        'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
        'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    )

    /**
     * base64加密
     */
    fun encode(from: ByteArray): String? {
        val to = StringBuilder((from.size * 1.34).toInt() + 3)
        var num = 0
        var currentByte = 0.toChar()
        for (i in from.indices) {
            num %= 8
            while (num < 8) {
                when (num) {
                    0 -> {
                        currentByte = (from[i].toInt() and lead6byte.code).toChar()
                        currentByte = (currentByte.code ushr 2).toChar()
                    }
                    2 -> currentByte = (from[i].toInt() and last6byte.code).toChar()
                    4 -> {
                        currentByte = (from[i].toInt() and last4byte.code).toChar()
                        currentByte = (currentByte.code shl 2).toChar()
                        if (i + 1 < from.size) {
                            currentByte =
                                (currentByte.code or (from[i + 1].toInt() and lead2byte.code ushr 6)).toChar()
                        }
                    }
                    6 -> {
                        currentByte = (from[i].toInt() and last2byte.code).toChar()
                        currentByte = (currentByte.code shl 4).toChar()
                        if (i + 1 < from.size) {
                            currentByte =
                                (currentByte.code or (from[i + 1].toInt() and lead4byte.code ushr 4)).toChar()
                        }
                    }
                    else -> {
                    }
                }
                to.append(encodeTable.get(currentByte.code))
                num += 6
            }
        }
        if (to.length % 4 != 0) {
            for (i in 4 - to.length % 4 downTo 1) {
                to.append("=")
            }
        }
        return to.toString()
    }

    /**
     * md5加密
     */
    fun md5(string: String): String? {
        if (TextUtils.isEmpty(string)) {
            return ""
        }
        val md5: MessageDigest
        try {
            md5 = MessageDigest.getInstance("MD5")
            val bytes = md5.digest(string.toByteArray())
            val result = StringBuilder()
            for (b in bytes) {
                var temp = Integer.toHexString(b.toInt() and 0xff)
                if (temp.length == 1) {
                    temp = "0$temp"
                }
                result.append(temp)
            }
            return result.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}