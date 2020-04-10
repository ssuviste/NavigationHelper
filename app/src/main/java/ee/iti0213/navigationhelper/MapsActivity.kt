package ee.iti0213.navigationhelper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.muddzdev.styleabletoast.StyleableToast
import kotlinx.android.synthetic.main.bottom_panel.*
import kotlinx.android.synthetic.main.cp_stats.*
import kotlinx.android.synthetic.main.start_stats.*
import kotlinx.android.synthetic.main.top_panel.*
import kotlinx.android.synthetic.main.wp_stats.*
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
        private var locationServiceActive = false
        private var compassEnabled = false
        private var upDirectionState = UpDirection.USER_CHOSEN
        private var keepCenteredEnabled = false
    }

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private lateinit var mMap: GoogleMap

    private lateinit var trackPolyline: Polyline
    private lateinit var trackPolylineOptions: PolylineOptions
    private var track = ArrayList<LatLng>()

    private var toastedBtnLastClickTime = 0L

    private lateinit var mBoundService: LocationService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationService.LocationBinder
            mBoundService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE)
        broadcastReceiverIntentFilter.addAction(C.TRACKING_UPDATE)

        createNotificationChannel()

        if (!checkPermissions()) {
            requestPermissions()
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
        unbindService(connection)
        mBound = false
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(C.TRACK_KEY, track)

        outState.putString(C.WALK_DIST_START_KEY, walkDistStart.text.toString())
        outState.putString(C.FLY_DIST_START_KEY, flyDistStart.text.toString())
        outState.putString(C.TIME_START_KEY, timeStart.text.toString())
        outState.putString(C.SPEED_START_KEY, speedStart.text.toString())

        outState.putString(C.WALK_DIST_CP_KEY, walkDistCP.text.toString())
        outState.putString(C.FLY_DIST_CP_KEY, flyDistCP.text.toString())
        outState.putString(C.TIME_CP_KEY, timeCP.text.toString())
        outState.putString(C.SPEED_CP_KEY, speedCP.text.toString())

        outState.putString(C.WALK_DIST_WP_KEY, walkDistWP.text.toString())
        outState.putString(C.FLY_DIST_WP_KEY, flyDistWP.text.toString())
        outState.putString(C.TIME_WP_KEY, timeWP.text.toString())
        outState.putString(C.SPEED_WP_KEY, speedWP.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)

        track = savedInstanceState.getParcelableArrayList(C.TRACK_KEY)!!

        walkDistStart.text =
            savedInstanceState.getString(C.WALK_DIST_START_KEY, R.string.init_dist.toString())
        flyDistStart.text =
            savedInstanceState.getString(C.FLY_DIST_START_KEY, R.string.init_dist.toString())
        timeStart.text =
            savedInstanceState.getString(C.TIME_START_KEY, R.string.init_time.toString())
        speedStart.text =
            savedInstanceState.getString(C.SPEED_START_KEY, R.string.init_speed.toString())

        walkDistCP.text =
            savedInstanceState.getString(C.WALK_DIST_CP_KEY, R.string.init_dist.toString())
        flyDistCP.text =
            savedInstanceState.getString(C.FLY_DIST_CP_KEY, R.string.init_dist.toString())
        timeCP.text =
            savedInstanceState.getString(C.TIME_CP_KEY, R.string.init_time.toString())
        speedCP.text =
            savedInstanceState.getString(C.SPEED_CP_KEY, R.string.init_speed.toString())

        walkDistWP.text =
            savedInstanceState.getString(C.WALK_DIST_WP_KEY, R.string.init_dist.toString())
        flyDistWP.text =
            savedInstanceState.getString(C.FLY_DIST_WP_KEY, R.string.init_dist.toString())
        timeWP.text =
            savedInstanceState.getString(C.TIME_WP_KEY, R.string.init_time.toString())
        speedWP.text =
            savedInstanceState.getString(C.SPEED_WP_KEY, R.string.init_speed.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == C.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.count() <= 0 -> { // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                    Toast.makeText(this, "User interaction was cancelled.", Toast.LENGTH_SHORT)
                        .show()
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> { // Permission was granted.
                    Log.i(TAG, "Permission granted.")
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show()
                }
                else -> { // Permission denied.
                    Snackbar.make(
                        findViewById(R.id.activity_maps),
                        "GPS permission denied, unable to continue.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Settings") {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri: Uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        mMap = googleMap
        trackPolylineOptions = PolylineOptions()
            .color(ContextCompat.getColor(this, R.color.colorGreenGlass))
        trackPolyline = mMap.addPolyline(trackPolylineOptions)

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        if (checkPermissions()) {
            mMap.isMyLocationEnabled = true
        }
        updateUI()
    }

    private fun createNotificationChannel() {
        // when on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                C.NOTIFICATION_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Default channel"

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Returns the current state of the permissions needed.
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_maps),
                "Access to GPS needed!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("OK") {
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    C.REQUEST_PERMISSIONS_REQUEST_CODE
                )
            }.show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                C.REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun updateLocationServiceState() {
        if (locationServiceActive) {
            if (Build.VERSION.SDK_INT >= 26) {
                // starting the FOREGROUND service
                // service has to display non-dismissible notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            showToastMsg(getString(R.string.tracking_started))
        } else {
            // stopping the service
            stopService(Intent(this, LocationService::class.java))
            showToastMsg(getString(R.string.tracking_stopped))
        }
    }

    // BROADCAST RECEIVER
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, intent!!.action!!)
            when (intent.action) {
                C.LOCATION_UPDATE -> {
                    val lat = intent.getDoubleExtra(C.LOC_UPD_LATITUDE_KEY, 0.0)
                    val lng = intent.getDoubleExtra(C.LOC_UPD_LONGITUDE_KEY, 0.0)
                    val target = if (keepCenteredEnabled && (lat != 0.0 || lng != 0.0)) {
                        LatLng(lat, lng)
                    } else {
                        mMap.cameraPosition.target
                    }
                    val bearing = when (upDirectionState) {
                        UpDirection.NORTH -> 0f
                        UpDirection.DIRECTION -> intent.getFloatExtra(C.LOC_UPD_BEARING_KEY, 0f)
                        UpDirection.USER_CHOSEN -> mMap.cameraPosition.bearing
                    }
                    if (target != mMap.cameraPosition.target || bearing != mMap.cameraPosition.bearing) {
                        val cameraPosition = CameraPosition(
                            target,
                            mMap.cameraPosition.zoom,
                            mMap.cameraPosition.tilt,
                            bearing
                        )
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    }
                }
                C.TRACKING_UPDATE -> {
                    val newTrack = intent.getParcelableArrayListExtra<LatLng>(C.TCK_UPD_TRACK_KEY)
                    if (newTrack != null) {
                        track = newTrack
                        updateTrack()
                    }

                    walkDistStart.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_WALK_DIST_START_KEY), getString(R.string.init_dist))
                    flyDistStart.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_FLY_DIST_START_KEY), getString(R.string.init_dist))
                    timeStart.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_TIME_START_KEY), getString(R.string.init_time))
                    speedStart.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_SPEED_START_KEY), getString(R.string.init_speed))

                    walkDistCP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_WALK_DIST_CP_KEY), getString(R.string.init_dist))
                    flyDistCP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_FLY_DIST_CP_KEY), getString(R.string.init_dist))
                    timeCP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_TIME_CP_KEY), getString(R.string.init_time))
                    speedCP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_SPEED_CP_KEY), getString(R.string.init_speed))

                    walkDistWP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_WALK_DIST_WP_KEY), getString(R.string.init_dist))
                    flyDistWP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_FLY_DIST_WP_KEY), getString(R.string.init_dist))
                    timeWP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_TIME_WP_KEY), getString(R.string.init_time))
                    speedWP.text = replaceNullString(intent.getStringExtra(C.TCK_UPD_SPEED_WP_KEY), getString(R.string.init_speed))
                }
            }
        }
    }

    private fun replaceNullString(input: String?, replacement: String): String {
        if (input == null) {
            return replacement
        }
        return input
    }

    private fun resetTrack() {
        track.clear()
        trackPolyline.points = track
    }

    private fun updateTrack() {
        trackPolyline.points = track
    }

    private fun resetBottomPanelStats() {
        walkDistStart.text = getString(R.string.init_dist)
        flyDistStart.text = getString(R.string.init_dist)
        timeStart.text = getString(R.string.init_time)
        speedStart.text = getString(R.string.init_speed)

        walkDistCP.text = getString(R.string.init_dist)
        flyDistCP.text = getString(R.string.init_dist)
        timeCP.text = getString(R.string.init_time)
        speedCP.text = getString(R.string.init_speed)

        walkDistWP.text = getString(R.string.init_dist)
        flyDistWP.text = getString(R.string.init_dist)
        timeWP.text = getString(R.string.init_time)
        speedWP.text = getString(R.string.init_speed)
    }

    private fun changeStartStopBtnIcon() {
        if (locationServiceActive) {
            buttonStartStop.setImageResource(R.drawable.baseline_stop_24)
        } else {
            buttonStartStop.setImageResource(R.drawable.baseline_play_arrow_24)
        }
    }

    private fun changeCompassBtnIcon() {
        if (compassEnabled) {
            buttonCompass.setImageResource(R.drawable.baseline_explore_24)
        } else {
            buttonCompass.setImageResource(R.drawable.baseline_explore_off_24)
        }
    }

    private fun changeUpDirBtnIcon() {
        when (upDirectionState) {
            UpDirection.USER_CHOSEN ->
                buttonUpDirection.setImageResource(R.drawable.baseline_emoji_people_24)
            UpDirection.DIRECTION ->
                buttonUpDirection.setImageResource(R.drawable.baseline_direction_indicator_24)
            UpDirection.NORTH ->
                buttonUpDirection.setImageResource(R.drawable.baseline_north_up_24)
        }
    }

    private fun changeKeepCenteredBtnIcon() {
        if (keepCenteredEnabled) {
            buttonKeepCentered.setImageResource(R.drawable.baseline_donut_small_24)
        } else {
            buttonKeepCentered.setImageResource(R.drawable.baseline_donut_large_24)
        }
    }

    private fun updateUI() {
        changeStartStopBtnIcon()
        changeCompassBtnIcon()
        changeUpDirBtnIcon()
        changeKeepCenteredBtnIcon()
        updateTrack()
    }

    private fun showToastMsg(msg: String) {
        StyleableToast.makeText(this, msg, Toast.LENGTH_SHORT, R.style.glassToast).show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonStartStopOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - toastedBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        toastedBtnLastClickTime = SystemClock.elapsedRealtime()

        locationServiceActive = !locationServiceActive
        updateLocationServiceState()
        changeStartStopBtnIcon()
        if (locationServiceActive) {
            resetTrack()
            resetBottomPanelStats()
            sendBroadcast(Intent(C.ENABLE_TRACKING))
        } else {
            sendBroadcast(Intent(C.DISABLE_TRACKING))
            //TODO: Save track
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonCompassOnClick(view: View) {
        compassEnabled = !compassEnabled
        changeCompassBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonUpDirOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - toastedBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        toastedBtnLastClickTime = SystemClock.elapsedRealtime()

        upDirectionState = when (upDirectionState) {
            UpDirection.USER_CHOSEN -> {
                showToastMsg(getString(R.string.up_dir_direction))
                UpDirection.DIRECTION
            }
            UpDirection.DIRECTION -> {
                showToastMsg(getString(R.string.up_dir_north))
                UpDirection.NORTH
            }
            UpDirection.NORTH -> {
                showToastMsg(getString(R.string.up_dir_user_chosen))
                UpDirection.USER_CHOSEN
            }
        }
        changeUpDirBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonKeepCenteredOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - toastedBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        toastedBtnLastClickTime = SystemClock.elapsedRealtime()

        keepCenteredEnabled = !keepCenteredEnabled
        if (keepCenteredEnabled) {
            showToastMsg(getString(R.string.center_enabled))
        } else {
            showToastMsg(getString(R.string.center_disabled))
        }
        changeKeepCenteredBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonCPOnClick(view: View) {
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_CP))
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonWPOnClick(view: View) {
        sendBroadcast(Intent(C.NOTIFICATION_ACTION_WP))
    }
}
