package ee.iti0213.navigationhelper

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*


class LocationService : Service() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private val mLocationRequest: LocationRequest = LocationRequest()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null

    // last received location
    private var currentLocation: Location? = null

    private var locationStart: Location? = null
    private var walkDistStart = 0f
    private var flyDistStart = 0f

    private var locationCP: Location? = null
    private var walkDistCP = 0f
    private var flyDistCP = 0f

    private var locationWP: Location? = null
    private var walkDistWP = 0f
    private var flyDistWP = 0f


    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_CP)
        broadcastReceiverIntentFilter.addAction(C.NOTIFICATION_ACTION_WP)
        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE_ACTION)

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

    private fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        try {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e(
                TAG,
                "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")
        if (currentLocation == null){
            locationStart = location
            locationCP = location
            locationWP = location
        } else {
            flyDistStart = location.distanceTo(locationStart)
            walkDistStart += location.distanceTo(currentLocation)

            flyDistCP = location.distanceTo(locationCP)
            walkDistCP += location.distanceTo(currentLocation)

            flyDistWP = location.distanceTo(locationWP)
            walkDistWP += location.distanceTo(currentLocation)
        }
        // save the location for calculations
        currentLocation = location

        //showNotification()

        // broadcast new location to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LATITUDE, location.latitude)
        intent.putExtra(C.LOCATION_UPDATE_ACTION_LONGITUDE, location.longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createLocationRequest() {
        mLocationRequest.interval = C.UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.fastestInterval = C.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.maxWaitTime = C.UPDATE_INTERVAL_IN_MILLISECONDS
    }


    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task -> if (task.isSuccessful) {
                    Log.w(TAG, "task successful")
                    if (task.result != null){
                        onNewLocation(task.result!!)
                    }
                } else {

                    Log.w(TAG, "Failed to get location." + task.exception)
                }}
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        //stop location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        // remove notifications
        NotificationManagerCompat.from(this).cancelAll()

        // don't forget to unregister broadcast receiver!!!!
        unregisterReceiver(broadcastReceiver)

        // broadcast stop to UI
        val intent = Intent(C.LOCATION_UPDATE_ACTION)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        // set counters and locations to 0/null
        currentLocation = null
        locationStart = null
        locationCP = null
        locationWP = null

        flyDistStart = 0f
        walkDistStart = 0f
        flyDistCP = 0f
        walkDistCP = 0f
        flyDistWP = 0f
        walkDistWP = 0f

        //showNotification()

        return START_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        TODO("not implemented")
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    fun showNotification(){
        val intentCp = Intent(C.NOTIFICATION_ACTION_CP)
        val intentWp = Intent(C.NOTIFICATION_ACTION_WP)

        val pendingIntentCp = PendingIntent.getBroadcast(this, 0, intentCp, 0)
        val pendingIntentWp = PendingIntent.getBroadcast(this, 0, intentWp, 0)

        val notifyView = RemoteViews(packageName, R.layout.notification_panel)

        notifyView.setOnClickPendingIntent(R.id.buttonCP, pendingIntentCp)
        notifyView.setOnClickPendingIntent(R.id.buttonWP, pendingIntentWp)

        notifyView.setTextViewText(R.id.walkDistStart, "%.2f".format(walkDistStart))
        notifyView.setTextViewText(R.id.flyDistStart, "%.2f".format(flyDistStart))

        notifyView.setTextViewText(R.id.walkDistCP, "%.2f".format(walkDistCP))
        notifyView.setTextViewText(R.id.flyDistCP, "%.2f".format(flyDistCP))

        notifyView.setTextViewText(R.id.walkDistWP, "%.2f".format(walkDistWP))
        notifyView.setTextViewText(R.id.flyDistWP, "%.2f".format(flyDistWP))

        // construct and show notification
        val builder = NotificationCompat.Builder(applicationContext, C.NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.baseline_explore_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        builder.setContent(notifyView)

        // Super important, start as foreground service - ie android considers this as an active app. Need visual reminder - notification.
        // must be called within 5 secs after service starts.
        startForeground(C.NOTIFICATION_ID, builder.build())
    }

    private inner class InnerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when(intent.action){
                C.NOTIFICATION_ACTION_WP -> {
                    locationWP = currentLocation
                    walkDistWP = 0f
                    flyDistWP = 0f
                    //showNotification()
                }
                C.NOTIFICATION_ACTION_CP -> {
                    locationCP = currentLocation
                    walkDistCP = 0f
                    flyDistCP = 0f
                    //showNotification()
                }
            }
        }
    }
}