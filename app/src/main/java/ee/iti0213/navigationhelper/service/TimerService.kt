package ee.iti0213.navigationhelper.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import ee.iti0213.navigationhelper.helper.C

class TimerService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private lateinit var handler: Handler
    private val sendTimerBroadcast = object : Runnable {
        override fun run() {
            sendBroadcast(Intent(C.TIMER_ACTION))
            handler.postDelayed(
                this,
                C.TIMER_INTERVAL_IN_MILLISECONDS
            )
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            sendTimerBroadcast,
            C.TIMER_INTERVAL_IN_MILLISECONDS
        )

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        handler.removeCallbacks(sendTimerBroadcast)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}