package ee.iti0213.navigationhelper.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import ee.iti0213.navigationhelper.R
import ee.iti0213.navigationhelper.db.LocationData
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.db.SessionData
import ee.iti0213.navigationhelper.helper.*

class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    // Binder given to clients
    private val binder = LocationBinder()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    private lateinit var databaseConnector: Repository

    private lateinit var sessionLocalId: String

    private var serviceStartTimestamp = 0L
    private var lastCPTimestamp = 0L
    private var lastWPTimestamp = 0L

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

        databaseConnector = Repository(this).open()

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
        sessionLocalId = Common.generateHashString()
        getLastLocation()
        createLocationRequest()
        requestLocationUpdates()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        // close database connection
        databaseConnector.close()

        // remove notifications
        cancelAllNotifications()

        // unregister broadcast receiver
        unregisterReceiver(broadcastReceiver)
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        trackingEnabled = true
        serviceStartTimestamp = System.currentTimeMillis()

        addSessionToDatabase()

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

    private fun addSessionToDatabase() {
        val existingLocalIds = databaseConnector.getAllSessionLocalIds()
        while (existingLocalIds.contains(sessionLocalId)) {
            sessionLocalId = Common.generateHashString()
        }
        databaseConnector.addSession(
            SessionData(
                sessionLocalId,
                if (Preferences.syncEnabled && State.loggedIn) null else C.LOCAL_SESSION,
                getString(R.string.auto_session_name),
                getString(R.string.auto_session_desc),
                System.currentTimeMillis() / 1000 * 1000,
                Preferences.gradientMinPace,
                Preferences.gradientMaxPace,
                0
            )
        )
    }

    private fun finishSessionInDatabase(newSessionName: String?) {
        databaseConnector.setSessionIsFinished(sessionLocalId, 1)
        if (!newSessionName.isNullOrBlank()) {
            databaseConnector.setSessionName(sessionLocalId, newSessionName)
        }
        if (currentLocation != null) {
            addLocationToDatabase(currentLocation!!, C.LOC_TYPE_LOC)
        }
    }

    private fun addLocationToDatabase(location: Location, locationType: String) {
        databaseConnector.addLocation(
            LocationData(
                sessionLocalId,
                location,
                System.currentTimeMillis() / 1000 * 1000,
                locationType,
                if (Preferences.syncEnabled && State.loggedIn) 1 else 0
            )
        )
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
                    Log.w(TAG, "Successfully got last location")
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
        if (!location.hasAccuracy()
            || location.accuracy > Preferences.gpsAccuracy
            || (currentLocation != null && currentLocation!!.distanceTo(location) < C.LOC_STAND_RADIUS)) {
            return
        }
        Log.i(TAG, "New location: $location")
        val intent = Intent(C.LOCATION_UPDATE)
        intent.putExtra(C.LOC_UPD_LOCATION_KEY, location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        if (trackingEnabled) {
            updateDistances(location)
            showTrack()
            addLocationToDatabase(location, C.LOC_TYPE_LOC)
        }
    }

    private fun updateDistances(location: Location) {
        if (currentLocation == null) {
            locationStart = location
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
        track.add(location)
        currentLocation = location
    }

    private fun updateTimes() {
        timeStart = (System.currentTimeMillis() - serviceStartTimestamp) / 1000
        if (locationCP != null) {
            timeCP = (System.currentTimeMillis() - lastCPTimestamp) / 1000
        }
        if (locationWP != null) {
            timeWP = (System.currentTimeMillis() - lastWPTimestamp) / 1000
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
        intent.putExtra(C.TCK_UPD_TIME_START_KEY, Common.formatTime(timeStart))
        intent.putExtra(
            C.TCK_UPD_SPEED_START_KEY, Common.formatSpeed(speedStart) + getString(
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
        intent.putExtra(C.TCK_UPD_TIME_CP_KEY, Common.formatTime(timeCP))
        intent.putExtra(
            C.TCK_UPD_SPEED_CP_KEY, Common.formatSpeed(speedCP) + getString(
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
        intent.putExtra(C.TCK_UPD_TIME_WP_KEY, Common.formatTime(timeWP))
        intent.putExtra(
            C.TCK_UPD_SPEED_WP_KEY, Common.formatSpeed(speedWP) + getString(
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
        notifyView.setTextViewText(R.id.timeStart, Common.formatTime(timeStart))
        notifyView.setTextViewText(
            R.id.speedStart, Common.formatSpeed(speedStart) + getString(R.string.unit_speed))

        notifyView.setTextViewText(
            R.id.walkDistCP, "%.0f".format(walkDistCP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(
            R.id.flyDistCP, "%.0f".format(flyDistCP) + getString(
                R.string.unit_dist
            ))
        notifyView.setTextViewText(R.id.timeCP, Common.formatTime(timeCP))
        notifyView.setTextViewText(
            R.id.speedCP, Common.formatSpeed(speedCP) + getString(
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
        notifyView.setTextViewText(R.id.timeWP, Common.formatTime(timeWP))
        notifyView.setTextViewText(
            R.id.speedWP, Common.formatSpeed(speedWP) + getString(
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

    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when(intent.action) {
                C.DISABLE_TRACKING -> {
                    val newSessionName = intent.getStringExtra(C.DIS_TCK_SESSION_NAME_KEY)
                    trackingEnabled = false
                    toggleTimerService(trackingEnabled)
                    cancelAllNotifications()
                    finishSessionInDatabase(newSessionName)
                }
                C.NOTIFICATION_ACTION_CP -> {
                    if (trackingEnabled && currentLocation != null) {
                        locationCP = currentLocation
                        addLocationToDatabase(locationCP!!, C.LOC_TYPE_CP)
                        lastCPTimestamp = System.currentTimeMillis()
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
                        addLocationToDatabase(locationWP!!, C.LOC_TYPE_WP)
                        lastWPTimestamp = System.currentTimeMillis()
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