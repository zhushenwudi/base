package com.zhushenwudi.base.network

/**
 * 作者　: hegaojian
 * 时间　: 2019/12/17
 * 描述　: 错误枚举类
 */
enum class Error(private val code: String, private val err: String) {

    UNKNOWN("1000", "请求失败，请稍后再试"),

    PARSE_ERROR("1001", "解析错误，请稍后再试"),

    NETWORK_ERROR("1002", "网络连接错误，请稍后重试"),

    SSL_ERROR("1003", "证书出错，请稍后再试"),

    TIMEOUT_ERROR("1004", "网络连接超时，请稍后重试"),

    AUTHOR_VERIFY_FAIL("1005", "认证失败，请重新登录"),

    SERVER_ERROR("1006", "服务繁忙，请稍后再试"),

    DNS_ERROR("1007", "网络域名配置错误"),

    NO_NET_ERROR("1008", "请先连接网络");

    fun getValue(): String {
        return err
    }

    fun getKey(): String {
        return code
    }

}