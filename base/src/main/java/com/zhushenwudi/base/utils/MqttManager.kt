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
        username: String? = null,
        password: String? = null,
        subscribeTopic: Array<String>? = null
    ) {
        try {
            mqttClient =
                MqttAndroidClient(appContext, url, Build.DEVICE + System.currentTimeMillis())
            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectionLost(cause: Throwable?) {
                    cause?.printStackTrace()
                    LogPrintUtils.d("mqtt disconnected")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    result.postValue(topic to message.toString())
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    LogPrintUtils.d("mqtt connected")
                    subscribeTopic?.run { subscribe(this) }
                }
            })

            val mqttConnectOptions = MqttConnectOptions()
            // 配置用户名密码
            username?.run { mqttConnectOptions.userName = this }
            password?.run { mqttConnectOptions.password = toCharArray() }
            // 设置心跳
            mqttConnectOptions.keepAliveInterval = 30
            // 设置自动重连
            mqttConnectOptions.isAutomaticReconnect = true
            // 设置建立连接时清空会话
            mqttConnectOptions.isCleanSession = false

            mqttClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.printStackTrace()
                    LogPrintUtils.d("mqtt connect fail")
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
            mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    LogPrintUtils.d("mqtt subscribe success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    LogPrintUtils.d("mqtt subscribe fail")
                }
            })
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
            mqttClient?.subscribe(topics, qosArray.toIntArray(), null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    LogPrintUtils.d("mqtt subscribe success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    LogPrintUtils.d("mqtt subscribe fail")
                }
            })
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
            mqttClient?.publish(topic, message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /**
     * 释放 mqtt 连接
     */
    fun release() {
        try {
            mqttClient?.disconnect()
            mqttClient = null
        } catch (e: Exception) {
        }
    }
}