package com.example.autoanswerapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
//built over threads which manage processes one by one
import android.content.pm.ServiceInfo
import android.os.Build

class CallDetectorService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var phoneStateListener: PhoneStateListener
    private lateinit var telecomManager: TelecomManager
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("CallDetectorService", "Service onCreate called")
        try{
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        // TODO: Implement auto-answer logic
                        Log.d("CallDetectorService", "Incoming call detected")
                        job = CoroutineScope(Dispatchers.Default).launch {
                            val delay = PreferenceManager.getAnswerDelay(this@CallDetectorService) * 1000L
                            Log.d("CallDetectorService", "Waiting for $delay ms before answering")
                            delay(delay)
                            if (PreferenceManager.isAutoAnswerEnabled(this@CallDetectorService)) {
                                answerCall()
                            } else {
                                Log.d("CallDetectorService", "Auto-answer is disabled")
                            }
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.d("CallDetectorService", "Call ended or rejected")
                        job?.cancel()
                    }
                }
            }
        }
    } catch (e:Exception)
        {
            Log.e("CallDetectorService", "Error in onCreate", e)

        }    }

    private fun answerCall() {
        try {
            Log.d("CallDetectorService", "Attempting to answer call")
            telecomManager.acceptRingingCall()
            Log.d("CallDetectorService", "Call answered successfully")
        } catch (e: Exception) {
            Log.e("CallDetectorService", "Error answering call", e)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CallDetectorService", "Service onStartCommand called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d("CallDetectorService", "Service onDestroy called")
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val channelId = "CallDetectorChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Call Detector Service", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Auto Answer Active")
            .setContentText("Listening for incoming calls")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
