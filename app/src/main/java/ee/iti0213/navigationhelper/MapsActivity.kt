package ee.iti0213.navigationhelper

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import ee.iti0213.navigationhelper.controller.CompassController
import ee.iti0213.navigationhelper.helper.*
import ee.iti0213.navigationhelper.service.LocationService
import ee.iti0213.navigationhelper.state.State
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.bottom_panel.*
import kotlinx.android.synthetic.main.cp_stats.*
import kotlinx.android.synthetic.main.start_stats.*
import kotlinx.android.synthetic.main.top_panel.*
import kotlinx.android.synthetic.main.wp_stats.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
        private var compassEnabled: Boolean = false
        private var upDirectionState: UpDirection = UpDirection.USER_CHOSEN
        private var keepCenteredEnabled: Boolean = false
    }

    private val broadcastReceiver = InnerBroadcastReceiver()
    private val broadcastReceiverIntentFilter: IntentFilter = IntentFilter()

    private lateinit var mMap: GoogleMap
    private lateinit var compass: CompassController

    private var track = ArrayList<Location>()
    private var trackPolyLinesCount = 0
    private var cpArray = ArrayList<Location>()
    private var cpMarkersCount = 0
    private var wp: Location? = null
    private var wpMarker: Marker? = null

    private lateinit var mBoundService: LocationService
    private var mBound: Boolean = false

    // Defines callbacks for service binding, passed to bindService()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocationService, cast the IBinder and get LocationService instance
            val binder = service as LocationService.LocationBinder
            mBoundService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        broadcastReceiverIntentFilter.addAction(C.LOCATION_UPDATE)
        broadcastReceiverIntentFilter.addAction(C.TRACKING_UPDATE)

        createNotificationChannel()

        if (!checkFineLocationPermission()) {
            requestFineLocationPermission()
        }
        if (!checkExtStoragePermission()) {
            requestExtStoragePermission()
        }

        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val image = findViewById<ImageView>(R.id.imageViewCompass)
        compass = CompassController(sensorManager, accelerometer, magnetometer, image)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, broadcastReceiverIntentFilter)
        compass.actOnResume()
    }

    override fun onPause() {
        super.onPause()
        compass.actOnPause()
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(C.RES_TRACK_KEY, track)
        outState.putParcelableArrayList(C.RES_CPS_KEY, cpArray)
        outState.putParcelable(C.RES_WP, wp)

        outState.putString(C.RES_WALK_DIST_START_KEY, walkDistStart.text.toString())
        outState.putString(C.RES_FLY_DIST_START_KEY, flyDistStart.text.toString())
        outState.putString(C.RES_TIME_START_KEY, timeStart.text.toString())
        outState.putString(C.RES_SPEED_START_KEY, speedStart.text.toString())

        outState.putString(C.RES_WALK_DIST_CP_KEY, walkDistCP.text.toString())
        outState.putString(C.RES_FLY_DIST_CP_KEY, flyDistCP.text.toString())
        outState.putString(C.RES_TIME_CP_KEY, timeCP.text.toString())
        outState.putString(C.RES_SPEED_CP_KEY, speedCP.text.toString())

        outState.putString(C.RES_WALK_DIST_WP_KEY, walkDistWP.text.toString())
        outState.putString(C.RES_FLY_DIST_WP_KEY, flyDistWP.text.toString())
        outState.putString(C.RES_TIME_WP_KEY, timeWP.text.toString())
        outState.putString(C.RES_SPEED_WP_KEY, speedWP.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        track = savedInstanceState.getParcelableArrayList(C.RES_TRACK_KEY)!!
        cpArray = savedInstanceState.getParcelableArrayList(C.RES_CPS_KEY)!!
        wp = savedInstanceState.getParcelable(C.RES_WP)

        walkDistStart.text =
            savedInstanceState.getString(C.RES_WALK_DIST_START_KEY, R.string.init_dist.toString())
        flyDistStart.text =
            savedInstanceState.getString(C.RES_FLY_DIST_START_KEY, R.string.init_dist.toString())
        timeStart.text =
            savedInstanceState.getString(C.RES_TIME_START_KEY, R.string.init_time.toString())
        speedStart.text =
            savedInstanceState.getString(C.RES_SPEED_START_KEY, R.string.init_speed.toString())

        walkDistCP.text =
            savedInstanceState.getString(C.RES_WALK_DIST_CP_KEY, R.string.init_dist.toString())
        flyDistCP.text =
            savedInstanceState.getString(C.RES_FLY_DIST_CP_KEY, R.string.init_dist.toString())
        timeCP.text =
            savedInstanceState.getString(C.RES_TIME_CP_KEY, R.string.init_time.toString())
        speedCP.text =
            savedInstanceState.getString(C.RES_SPEED_CP_KEY, R.string.init_speed.toString())

        walkDistWP.text =
            savedInstanceState.getString(C.RES_WALK_DIST_WP_KEY, R.string.init_dist.toString())
        flyDistWP.text =
            savedInstanceState.getString(C.RES_FLY_DIST_WP_KEY, R.string.init_dist.toString())
        timeWP.text =
            savedInstanceState.getString(C.RES_TIME_WP_KEY, R.string.init_time.toString())
        speedWP.text =
            savedInstanceState.getString(C.RES_SPEED_WP_KEY, R.string.init_speed.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == C.REQUEST_FINE_LOC_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.count() <= 0 -> { // If user interaction was interrupted,
                    // the permission request is cancelled and we receive empty arrays.
                    Log.i(TAG, getString(R.string.user_ia_cancelled))
                    Common.showToastMsg(this, getString(R.string.user_ia_cancelled))
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> { // Permission was granted.
                    Log.i(TAG, getString(R.string.permission_granted))
                    Common.showToastMsg(this, getString(R.string.permission_granted))
                }
                else -> { // Permission denied.
                    Snackbar.make(
                        findViewById(R.id.activity_maps),
                        getString(R.string.gps_permission_denied),
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(getString(R.string.settings)) {
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
        mMap = googleMap
        if (checkFineLocationPermission()) {
            mMap.isMyLocationEnabled = true
        }
        updateMap()
        updateUI()
    }

    private fun createNotificationChannel() {
        // When on 8 Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                C.NOTIFICATION_CHANNEL,
                C.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = C.NOTIFICATION_CHANNEL_DESC

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Returns the current state of the permissions needed.
    private fun checkFineLocationPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestFineLocationPermission() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Snackbar.make(
                findViewById(R.id.activity_maps),
                getString(R.string.gps_needed),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.ok)) {
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    C.REQUEST_FINE_LOC_PERMISSIONS_REQUEST_CODE
                )
            }.show()
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                C.REQUEST_FINE_LOC_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun checkExtStoragePermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun requestExtStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            C.REQUEST_EXT_STORAGE_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun updateSessionState() {
        if (!State.sessionActive) {
            if (Build.VERSION.SDK_INT >= 26) {
                // Start the Foreground service
                // Service has to display non-dismissible notification within 5 secs
                startForegroundService(Intent(this, LocationService::class.java))
            } else {
                startService(Intent(this, LocationService::class.java))
            }
            Common.showToastMsg(this, getString(R.string.session_started))
            State.sessionActive = true
            changeStartStopBtnIcon()
            resetBottomPanelStats()
            resetTrack()
        } else {
            showStopTrackingConfirmationDialog()
        }
    }

    private fun showStopTrackingConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.end_session_question)

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val input = EditText(this)
        params.setMargins(52, 24, 52, 24)
        input.layoutParams = params
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = getString(R.string.session_title_hint)
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton(getString(R.string.confirm)) { _, _ ->
            run {
                val intent = Intent(C.DISABLE_TRACKING)
                intent.putExtra(C.DIS_TCK_SESSION_NAME_KEY, input.text.toString())
                sendBroadcast(intent)

                stopService(Intent(this, LocationService::class.java))
                State.sessionActive = false
                Common.showToastMsg(this, getString(R.string.session_ended_saved))
                changeStartStopBtnIcon()
                updateMap()
            }
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            run {
                dialog.cancel()
            }
        }

        builder.show()
    }

    // BROADCAST RECEIVER
    private inner class InnerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                C.LOCATION_UPDATE -> {
                    val loc = intent.extras!!.get(C.LOC_UPD_LOCATION_KEY) as Location
                    updateCamera(loc)
                }
                C.TRACKING_UPDATE -> {
                    val updTrack = intent.getParcelableArrayListExtra<Location>(C.TCK_UPD_TRACK_KEY)
                    val updAllCPs = intent.getParcelableArrayListExtra<Location>(C.TCK_UPD_CPS_KEY)
                    val updLocationWP = intent.getParcelableExtra<Location?>(C.TCK_UPD_WP_KEY)
                    if (updTrack != null && updAllCPs != null) {
                        track = updTrack
                        cpArray = updAllCPs
                    }
                    if (updLocationWP != null) {
                        wp = updLocationWP
                    }
                    updateMap()

                    walkDistStart.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_WALK_DIST_START_KEY),
                        getString(R.string.init_dist)
                    )
                    flyDistStart.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_FLY_DIST_START_KEY),
                        getString(R.string.init_dist)
                    )
                    timeStart.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_TIME_START_KEY),
                        getString(R.string.init_time)
                    )
                    speedStart.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_SPEED_START_KEY),
                        getString(R.string.init_speed)
                    )

                    walkDistCP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_WALK_DIST_CP_KEY),
                        getString(R.string.init_dist)
                    )
                    flyDistCP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_FLY_DIST_CP_KEY),
                        getString(R.string.init_dist)
                    )
                    timeCP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_TIME_CP_KEY),
                        getString(R.string.init_time)
                    )
                    speedCP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_SPEED_CP_KEY),
                        getString(R.string.init_speed)
                    )

                    walkDistWP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_WALK_DIST_WP_KEY),
                        getString(R.string.init_dist)
                    )
                    flyDistWP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_FLY_DIST_WP_KEY),
                        getString(R.string.init_dist)
                    )
                    timeWP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_TIME_WP_KEY),
                        getString(R.string.init_time)
                    )
                    speedWP.text = replaceNullString(
                        intent.getStringExtra(C.TCK_UPD_SPEED_WP_KEY),
                        getString(R.string.init_speed)
                    )
                }
            }
        }
    }

    private fun updateCamera(loc: Location) {
        val target = if (keepCenteredEnabled && (loc.latitude != 0.0 || loc.longitude != 0.0)) {
            LatLng(loc.latitude, loc.longitude)
        } else {
            mMap.cameraPosition.target
        }
        val bearing = when (upDirectionState) {
            UpDirection.NORTH -> 0f
            UpDirection.DIRECTION -> loc.bearing
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

    private fun updateMap() {
        updateMapMarkers()
        updateTrack()
    }

    private fun updateTrack() {
        while (track.size - 1 > trackPolyLinesCount) {
            val first =
                LatLng(track[trackPolyLinesCount].latitude, track[trackPolyLinesCount].longitude)
            val second = LatLng(
                track[trackPolyLinesCount + 1].latitude,
                track[trackPolyLinesCount + 1].longitude
            )
            val color = Common.getTrackColor(
                this,
                track[trackPolyLinesCount].time,
                track[trackPolyLinesCount + 1].time,
                track[trackPolyLinesCount].distanceTo(track[trackPolyLinesCount + 1])
            )
            mMap.addPolyline(PolylineOptions().add(first, second).color(color).width(C.TRACK_WIDTH))
            trackPolyLinesCount++
        }
    }

    private fun updateMapMarkers() {
        if (trackPolyLinesCount == 0 && track.size > 0) {
            val marker = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_fiber_manual_record_black_18))
                .position(LatLng(track[0].latitude, track[0].longitude))
                .anchor(0.5f, 0.5f)
            mMap.addMarker(marker)
        }
        if (!State.sessionActive && track.size > 0) {
            val marker = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_flag_black_36))
                .position(LatLng(track.last().latitude, track.last().longitude))
                .anchor(0.25f, 0.85f)
            mMap.addMarker(marker)
        }
        while (cpArray.size > cpMarkersCount) {
            val marker = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_beenhere_black_36))
                .position(
                    LatLng(
                        cpArray[cpMarkersCount].latitude,
                        cpArray[cpMarkersCount].longitude
                    )
                )
                .anchor(0.5f, 0.9f)
            mMap.addMarker(marker)
            cpMarkersCount++
        }
        if (wp != null) {
            if (wpMarker == null) {
                val marker = MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_36))
                    .anchor(0.5f, 0.9f)
                wpMarker = mMap.addMarker(
                    marker
                        .position(LatLng(wp!!.latitude, wp!!.longitude))
                )
            } else if (wpMarker!!.position.latitude != wp!!.latitude
                || wpMarker!!.position.longitude != wp!!.longitude
            ) {
                val marker = MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_36))
                    .anchor(0.5f, 0.9f)
                wpMarker!!.remove()
                wpMarker = mMap.addMarker(
                    marker
                        .position(LatLng(wp!!.latitude, wp!!.longitude))
                )
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
        trackPolyLinesCount = 0
        cpArray.clear()
        cpMarkersCount = 0
        wp = null
        mMap.clear()
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
        if (State.sessionActive) {
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

    private fun toggleCompass() {
        compass.setVisible(compassEnabled)
        if (!compassEnabled) {
            imageViewCompass.clearAnimation()
            imageViewCompass.visibility = View.INVISIBLE
        }
    }

    private fun updateUI() {
        changeStartStopBtnIcon()
        changeCompassBtnIcon()
        changeUpDirBtnIcon()
        changeKeepCenteredBtnIcon()
        toggleCompass()
        updateMap()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonCompassOnClick(view: View) {
        compassEnabled = !compassEnabled
        toggleCompass()
        changeCompassBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonUpDirOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        upDirectionState = when (upDirectionState) {
            UpDirection.USER_CHOSEN -> {
                Common.showToastMsg(this, getString(R.string.up_dir_direction))
                UpDirection.DIRECTION
            }
            UpDirection.DIRECTION -> {
                Common.showToastMsg(this, getString(R.string.up_dir_north))
                UpDirection.NORTH
            }
            UpDirection.NORTH -> {
                Common.showToastMsg(this, getString(R.string.up_dir_user_chosen))
                UpDirection.USER_CHOSEN
            }
        }
        changeUpDirBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonKeepCenteredOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        keepCenteredEnabled = !keepCenteredEnabled
        if (keepCenteredEnabled) {
            Common.showToastMsg(this, getString(R.string.center_enabled))
        } else {
            Common.showToastMsg(this, getString(R.string.center_disabled))
        }
        changeKeepCenteredBtnIcon()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonStartStopOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        updateSessionState()
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
