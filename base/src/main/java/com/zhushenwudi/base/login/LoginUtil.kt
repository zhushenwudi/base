package com.zhushenwudi.base.login

import com.alley.openssl.OpensslUtil.getEncodeKey
import kotlin.Throws
import com.zhushenwudi.base.login.codec.binary.Base64
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URLEncoder

object LoginUtil {
    @Throws(Exception::class)
    fun getEnCodePwd(password: String, key: String = getEncodeKey()): String {
        return Aes.aesEncrypt(password, getSecret(key))
    }

    @Throws(Exception::class)
    fun getBToAEnCodePwd(password: String, key: String = getEncodeKey()): String {
        return BtoAAtoB.btoa(encodeURIComponent(Aes.aesEncrypt(password, getSecret(key))))
    }

    private fun encodeURIComponent(s: String?): String? {
        if (s == null) {
            return null
        }
        val result: String? = try {
            URLEncoder.encode(s, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            s
        }
        return result
    }

    private fun getSecret(username: String): String {
        val sb = StringBuilder(username)
        var base = Base64.encodeBase64String(username.toByteArray()).trim { it <= ' ' }
        while (base.length < 16) {
            sb.append("_")
            base = Base64.encodeBase64String(sb.toString().toByteArray()).trim { it <= ' ' }
        }
        return base.substring(base.length - 16)
    }
}