package com.zhushenwudi.base.utils

import android.annotation.SuppressLint
import android.os.Build
import com.zhushenwudi.base.app.appContext
import com.zhushenwudi.base.livedata.event.EventLiveData
import dev.utils.LogPrintUtils
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

object MqttManager {
    @SuppressLint("StaticFieldLeak")
    var mqttClient: MqttAndroidClient? = null

    /**
     * 连接 MQTT
     * @param url mqtt 地址
     * @param result 接收的消息
     * @param subscribeTopic 连接成功后要自动订阅的 topic
     */
    fun connect(
        url: String,
        result: EventLiveData<Pair<String, String>>,
        callback: (Boolean) -> Unit,
        username: String? = null,
        password: String? = null,
        subscribeTopic: Array<String>? = null
    ) {
        try {
            mqttClient = MqttAndroidClient(appContext, url, Build.SERIAL)
            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    cause?.printStackTrace()
                    callback(false)
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    result.postValue(topic to message.toString())
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    callback(true)
                    subscribeTopic?.run { subscribe(this) }
                }
            })

            val mqttConnectOptions = MqttConnectOptions()
            // 配置用户名密码
            username?.run { mqttConnectOptions.userName = this }
            password?.run { mqttConnectOptions.password = toCharArray() }
            // 设置心跳
            mqttConnectOptions.keepAliveInterval = 20
            // 设置自动重连
            mqttConnectOptions.isAutomaticReconnect = true
            // 设置建立连接时清空会话
            mqttConnectOptions.isCleanSession = true
            mqttConnectOptions.connectionTimeout = 10

            mqttClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    if (exception.toString().contains("已连接客户机")) {
                        callback(true)
                    } else {
                        callback(false)
                        exception?.printStackTrace()
                    }
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 订阅某个主题
     * @param topic 主题
     * @param qos 连接方式
     */
    fun subscribe(topic: String, qos: Int = 0) {
        try {
            mqttClient?.subscribe(topic, qos, null, null)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 订阅多主题
     * @param topics 主题数组
     * @param qos 连接方式
     */
    fun subscribe(topics: Array<String>, qos: IntArray? = null) {
        val qosArray = qos?.asList() ?: kotlin.run {
            val temp = arrayListOf<Int>()
            topics.map {
                temp.add(0)
            }
            temp
        }
        try {
            mqttClient?.subscribe(topics, qosArray.toIntArray(), null, null)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 发布 topic 消息
     */
    fun publish(topic: String, msg: String) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            LogPrintUtils.d("发布的主题: $topic")
            mqttClient?.publish(topic, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消订阅
     *
     * @param topic 多主题变长参数
     */
    fun cancelSubscribe(vararg topic: String) {
        try {
            mqttClient?.unsubscribe(topic)
        } catch (e: MqttException) {
        }
    }

    /**
     * 释放 mqtt 连接
     */
    fun release() {
        try {
            mqttClient?.disconnect()
            mqttClient = null
        } catch (e: MqttException) {
        }
    }
}