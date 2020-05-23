package ee.iti0213.navigationhelper.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.R

class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // Binder given to clients
    private val binder = LocationBinder()

    private val timeIntervalInSeconds: Long = C.TIMER_INTERVAL_IN_MILLISECONDS / 1000

    // last received location
    private var currentLocation: Location? = null

    private var trackingEnabled = false
    private var track = ArrayList<Location>()
    private var allCPs = ArrayList<Location>()

    private var locationStart: Location? = null
    private var walkDistStart = 0f
    private var flyDistStart = 0f
    private var timeStart = 0L
    private var speedStart = 0L

    private var locationCP: Location? = null
    private var walkDistCP = 0f
    private var flyDistCP = 0f
    private var timeCP = 0L
    private var speedCP = 0L

    private var locationWP: Location? = null
    private var walkDistWP = 0f
    private var flyDistWP = 0f
    private var timeWP = 0L
    private var speedWP = 0L


    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        broadcastReceiverIntentFilter.addAction(C.DISABLE_TRACKING)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.TIMER_ACTION)

        registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        getLastLocation()
        createLocationRequest()
        requestLocationUpdates()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        // remove notifications
        cancelAllNotifications()

        // don't forget to unregister broadcast receiver!!!!
        unregisterReceiver(broadcastReceiver)

        // broadcast stop to UI
        //val intent = Intent(C.TRACKING_UPDATE)
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        trackingEnabled = true

        // set counters and locations to initial state
        currentLocation = null
        locationStart = null
        locationCP = null
        locationWP = null

        track.clear()
        allCPs.clear()

        walkDistStart = 0f
        flyDistStart = 0f
        timeStart = 0L
        speedStart = 0L

        walkDistCP = 0f
        flyDistCP = 0f
        timeCP = 0L
        speedCP = 0L

        walkDistWP = 0f
        flyDistWP = 0f
        timeWP = 0L
        speedWP = 0L

        toggleTimerService(trackingEnabled)
        showNotification()

        return START_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    private fun createLocationRequest() {
        mLocationRequest.interval = C.LOC_UPD_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = C.LOC_FASTEST_UPD_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = C.LOC_UPD_INTERVAL_IN_MILLISECONDS
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task -> if (task.isSuccessful) {
                    Log.w(TAG, "Task successful")
                    if (task.result != null) {
                        onNewLocation(task.result!!)
                    }
                } else {
                    Log.w(TAG, "Failed to get location." + task.exception)
                }}
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission. $unlikely")
        }
    }

    private fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    private fun onNewLocation(location: Location) {
        //TODO: Set accuracy filtering
        if (!location.hasAccuracy()
            || location.accuracy > C.LOC_MIN_ACCURACY
            || (currentLocation != null && currentLocation!!.distanceTo(location) < C.LOC_STAND_RADIUS)) {
            return
        }
        Log.i(TAG, "New location: $location")
        val intent = Intent(C.LOCATION_UPDATE)
        intent.putExtra(C.LOC_UPD_LOCATION_KEY, location)

        intent.putExtra(C.LOC_UPD_LATITUDE_KEY, location.latitude)
        intent.putExtra(C.LOC_UPD_LONGITUDE_KEY, location.longitude)
        intent.putExtra(C.LOC_UPD_BEARING_KEY, location.bearing)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        if (trackingEnabled) {
            updateDistances(location)
            showTrack()
        }
    }

    private fun updateDistances(location: Location) {
        if (currentLocation == null) {
            locationStart = location
            //locationCP = location
            //locationWP = location
        } else {
            flyDistStart = location.distanceTo(locationStart)
            walkDistStart += location.distanceTo(currentLocation)

            if (locationCP != null) {
                flyDistCP = location.distanceTo(locationCP)
                walkDistCP += location.distanceTo(currentLocation)
            }

            if (locationWP != null) {
                flyDistWP = location.distanceTo(locationWP)
                walkDistWP += location.distanceTo(currentLocation)
            }
        }
        //track.add(LatLng(location.latitude, location.longitude))
        track.add(location)
        // keep the location for future
        currentLocation = location
    }

    private fun updateTimes() {
        timeStart += timeIntervalInSeconds
        if (locationCP != null) {
            timeCP += timeIntervalInSeconds
        }
        if (locationWP != null) {
            timeWP += timeIntervalInSeconds
        }
    }

    private fun updateSpeeds() {
        if (locationStart != null && walkDistStart != 0f) {
            speedStart = (timeStart / (walkDistStart / 1000)).toLong()
        }
        if (locationCP != null && walkDistCP != 0f) {
            speedCP = (timeCP / (walkDistCP / 1000)).toLong()
        }
        if (locationWP != null && walkDistWP != 0f) {
            speedWP = (timeWP / (walkDistWP / 1000)).toLong()
        }
    }

    private fun showTrack() {
        showNotification()

        // broadcast new location to UI
        val intent = Intent(C.TRACKING_UPDATE)
        intent.putExtra(C.TCK_UPD_TRACK_KEY, track)
        intent.putExtra(C.TCK_UPD_CPS_KEY, allCPs)
        intent.putExtra(C.TCK_UPD_WP_KEY, locationWP)

        intent.putExtra(
            C.TCK_UPD_WALK_DIST_START_KEY, "%.0f".format(walkDistStart) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(
            C.TCK_UPD_FLY_DIST_START_KEY, "%.0f".format(flyDistStart) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(C.TCK_UPD_TIME_START_KEY, formatTime(timeStart))
        intent.putExtra(
            C.TCK_UPD_SPEED_START_KEY, formatSpeed(speedStart) + getString(
                R.string.unit_speed
            ))

        intent.putExtra(
            C.TCK_UPD_WALK_DIST_CP_KEY, "%.0f".format(walkDistCP) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(
            C.TCK_UPD_FLY_DIST_CP_KEY, "%.0f".format(flyDistCP) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(C.TCK_UPD_TIME_CP_KEY, formatTime(timeCP))
        intent.putExtra(
            C.TCK_UPD_SPEED_CP_KEY, formatSpeed(speedCP) + getString(
                R.string.unit_speed
            ))

        intent.putExtra(
            C.TCK_UPD_WALK_DIST_WP_KEY, "%.0f".format(walkDistWP) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(
            C.TCK_UPD_FLY_DIST_WP_KEY, "%.0f".format(flyDistWP) + getString(
                R.string.unit_dist
            ))
        intent.putExtra(C.TCK_UPD_TIME_WP_KEY, formatTime(timeWP))
        intent.putExtra(
            C.TCK_UPD_SPEED_WP_KEY, formatSpeed(speedWP) + getString(
                R.string.unit_speed
            ))

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun showNotification() {
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        val notifyView = RemoteViews(packageName, R.layout.notification_panel)

        notifyView.setOnClickPendingIntent(R.id.buttonCP, pendingIntentCp)
        notifyView.setOnClickPendingIntent(R.id.buttonWP, pendingIntentWp)

        notifyView.setTextViewText(
            R.id.walkDistStart, "%.0f".format(walkDistStart) + getString(R.string.unit_dist))
        notifyView.setTextViewText(
            R.id.flyDistStart, "%.0f".format(flyDistStart) + getString(R.string.unit_dist))
        notifyView.setTextViewText(R.id.timeStart, formatTime(timeStart))
        notifyView.setTextViewText(
            R.id.speedStart, formatSpeed(speedStart) + getString(R.string.unit_speed))

        notifyView.setTextViewText(
            R.id.walkDistCP, "%.0f".format(walkDistCP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(
            R.id.flyDistCP, "%.0f".format(flyDistCP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(R.id.timeCP, formatTime(timeCP))
        notifyView.setTextViewText(
            R.id.speedCP, formatSpeed(speedCP) + getString(
                R.string.unit_speed
            ))

        notifyView.setTextViewText(
            R.id.walkDistWP, "%.0f".format(walkDistWP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(
            R.id.flyDistWP, "%.0f".format(flyDistWP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(R.id.timeWP, formatTime(timeWP))
        notifyView.setTextViewText(
            R.id.speedWP, formatSpeed(speedWP) + getString(
                R.string.unit_speed
            ))

        // construct and show notification
        val builder = NotificationCompat.Builder(applicationContext, C.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_gps_fixed_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomBigContentView(notifyView)

        // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
        // must be called within 5 secs after service starts.
        startForeground(C.NOTIFICATION_ID, builder.build())
    }

    private fun cancelAllNotifications() {
        NotificationManagerCompat.from(this).cancelAll()
        stopForeground(true)
    }

    private fun toggleTimerService(start: Boolean) {
        if (start) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(Intent(this, TimerService::class.java))
            } else {
                startService(Intent(this, TimerService::class.java))
            }
        } else {
            stopService(Intent(this, TimerService::class.java))
        }
    }

    private fun formatTime(time: Long): String {
        val hours = time / 3600
        if (hours > 99) return "99:59:59"
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        val hoursString = when {
            hours < 10 -> "0$hours"
            else -> "$hours"
        }
        val minutesString = when {
            minutes < 10 -> ":0$minutes"
            else -> ":$minutes"
        }
        val secondsString = when {
            seconds < 10 -> ":0$seconds"
            else -> ":$seconds"
        }
        return "$hoursString$minutesString$secondsString"
    }

    private fun formatSpeed(time: Long): String {
        val minutes = time / 60
        if (minutes > 99) return "99:59"
        val seconds = time % 60
        val minutesString = when {
            minutes < 10 -> "0$minutes"
            else -> "$minutes"
        }
        val secondsString = when {
            seconds < 10 -> ":0$seconds"
            else -> ":$seconds"
        }
        return "$minutesString$secondsString"
    }

    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when(intent.action) {
                C.DISABLE_TRACKING -> {
                    trackingEnabled = false
                    toggleTimerService(trackingEnabled)
                    cancelAllNotifications()
                }
                C.NOTIFICATION_ACTION_CP -> {
                    if (trackingEnabled && currentLocation != null) {
                        locationCP = currentLocation
                        allCPs.add(currentLocation!!)
                        walkDistCP = 0f
                        flyDistCP = 0f
                        timeCP = 0L
                        speedCP = 0L
                        showTrack()
                    }
                }
                C.NOTIFICATION_ACTION_WP -> {
                    if (trackingEnabled && currentLocation != null) {
                        locationWP = currentLocation
                        walkDistWP = 0f
                        flyDistWP = 0f
                        timeWP = 0L
                        speedWP = 0L
                        showTrack()
                    }
                }
                C.TIMER_ACTION -> {
                    if (trackingEnabled) {
                        updateTimes()
                        updateSpeeds()
                        showTrack()
                    }
                }
            }
        }
    }

    inner class LocationBinder : Binder() {
        // Return this instance of LocationService so clients can call public methods
        fun getService(): LocationService = this@LocationService
    }
}