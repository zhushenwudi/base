package com.zhushenwudi.base.utils

import android.content.Context
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import io.sentry.event.EventBuilder
import io.sentry.event.Event
import io.sentry.event.interfaces.ExceptionInterface

object SentryUtil {

    /**
     * Use the Sentry DSN (client key) from the Project Settings page on Sentry
     * String sentryDsn = "https://publicKey:secretKey@host:port/1?options";
     * String sentryDsn = "http://8c4a32c102b04f929f21356d7188c7c5:40945f7672654e949f572c98bf942fb2@10.10.19.203:9000/2";
     */
    //初始化sentry
    fun init(context: Context?, sentryDsn: String?) {
        Sentry.init(sentryDsn, AndroidSentryClientFactory(context))
    }

    //主动发送Throwable消息
    fun sendSentryException(throwable: Throwable?) {
        Sentry.capture(throwable)
    }

    //主动发送Event消息
    fun sendSentryException(throwable: Event?) {
        Sentry.capture(throwable)
    }

    //主动发送EventBuilder消息
    fun sendSentryException(throwable: EventBuilder?) {
        Sentry.capture(throwable)
    }

    fun sendSentryException(logger: String?, throwable: Throwable?) {
        sendSentryException(
            EventBuilder().withMessage("try catch msg").withLevel(Event.Level.WARNING)
                .withLogger(logger).withSentryInterface(ExceptionInterface(throwable))
        )
    }
}