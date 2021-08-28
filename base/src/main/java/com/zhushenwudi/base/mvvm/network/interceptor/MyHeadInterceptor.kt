package com.zhushenwudi.base.mvvm.network.interceptor

import com.zhushenwudi.base.mvvm.util.SpUtils
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MyHeadInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.method() == "GET") {
            //添加GET 请求公共参数
            val httpUrlBuilder = request.url().newBuilder()
            request = addCommonParameter(httpUrlBuilder, request)
        }

        val builder = request.newBuilder()
        //添加Header公共参数
        addHeaderParameter(builder)
        return chain.proceed(builder.build())
    }

    /**
     * 添加公共Header参数
     */
    private fun addHeaderParameter(builder: Request.Builder) {
        builder.addHeader(BEARER_KEY, BEARER_VALUE + SpUtils.getString("token"))
    }

    /**
     * 添加GET请求公共参数
     */
    private fun addCommonParameter(
        httpUrlBuilder: HttpUrl.Builder,
        request: Request
    ): Request {
        return request.newBuilder().url(httpUrlBuilder.build()).build()
    }

    companion object {
        const val BEARER_KEY = "X-Authorization"
        const val BEARER_VALUE = "Bearer "
    }
}