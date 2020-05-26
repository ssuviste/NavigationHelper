package ee.iti0213.navigationhelper

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import ee.iti0213.navigationhelper.db.LocationData
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.helper.DateFormat
import ee.iti0213.navigationhelper.helper.State
import kotlinx.android.synthetic.main.cp_stats_history.*
import kotlinx.android.synthetic.main.start_stats_history.*
import kotlinx.android.synthetic.main.wp_stats_history.*
import java.io.File
import java.io.FileWriter


class HistoryMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var sessionLocalId: String
    private lateinit var databaseConnector: Repository
    private lateinit var mMap: GoogleMap

    private lateinit var locations: List<LocationData>
    private lateinit var allCPs: List<LocationData>
    private lateinit var allWPs: List<LocationData>

    private var walkDistStart = 0f
    private var flyDistStart = 0f
    private var timeStart = 0L
    private var speedStart = 0L

    private var walkDistCP = 0f
    private var flyDistCP = 0f
    private var timeCP = 0L
    private var speedCP = 0L

    private var walkDistWP = 0f
    private var flyDistWP = 0f
    private var timeWP = 0L
    private var speedWP = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sessionLocalId = intent.getStringExtra(C.SESSION_LOCAL_ID_KEY)!!
        databaseConnector = Repository(this).open()

        locations = databaseConnector.getAllLocationsBySessionLocalId(sessionLocalId)
        allCPs = databaseConnector.getAllCPsBySessionLocalId(sessionLocalId)
        allWPs = databaseConnector.getAllWPsBySessionLocalId(sessionLocalId)

        calculateStatistics()
        displayStatistics()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawTrack()
        drawMapMarkers()
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(locations.first().latitude, locations.first().longitude),
                15f
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseConnector.close()
    }

    private fun calculateStatistics() {
        calculateDistances()
        calculateTimes()
        calculateSpeeds()
    }

    private fun calculateDistances() {
        if (locations.isNotEmpty()) {
            flyDistStart = calculateFlyDistance(locations.first())
            walkDistStart = calculateWalkDistance(locations.first())
            if (allCPs.isNotEmpty()) {
                flyDistCP = calculateFlyDistance(allCPs.last())
                walkDistCP = calculateWalkDistance(allCPs.last())
            }
            if (allWPs.isNotEmpty()) {
                flyDistWP = calculateFlyDistance(allWPs.last())
                walkDistWP = calculateWalkDistance(allWPs.last())
            }
        }
    }

    private fun calculateFlyDistance(start: LocationData): Float {
        val result = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            locations.last().latitude, locations.last().longitude, result
        )
        return result[0]
    }

    private fun calculateWalkDistance(start: LocationData): Float {
        var result = 0f
        var currLoc = start
        for (loc in locations) {
            if (loc.recordedAt > currLoc.recordedAt) {
                val calcResult = FloatArray(1)
                Location.distanceBetween(
                    currLoc.latitude, currLoc.longitude,
                    loc.latitude, loc.longitude, calcResult
                )
                result += calcResult[0]
                currLoc = loc
            }
        }
        return result
    }

    private fun calculateTimes() {
        if (locations.isNotEmpty()) {
            timeStart = (locations.last().recordedAt - locations.first().recordedAt) / 1000
            if (allCPs.isNotEmpty()) {
                timeCP = (locations.last().recordedAt - allCPs.last().recordedAt) / 1000
            }
            if (allWPs.isNotEmpty()) {
                timeWP = (locations.last().recordedAt - allWPs.last().recordedAt) / 1000
            }
        }
    }

    private fun calculateSpeeds() {
        if (walkDistStart != 0f) {
            speedStart = (timeStart / (walkDistStart / 1000)).toLong()
        }
        if (walkDistCP != 0f) {
            speedCP = (timeCP / (walkDistCP / 1000)).toLong()
        }
        if (walkDistWP != 0f) {
            speedWP = (timeWP / (walkDistWP / 1000)).toLong()
        }
    }

    private fun displayStatistics() {
        val walkDistStartString = "%.0f".format(walkDistStart) + getString(R.string.unit_dist)
        walkDistStartHistory.text = walkDistStartString
        val flyDistStartString = "%.0f".format(flyDistStart) + getString(R.string.unit_dist)
        flyDistStartHistory.text = flyDistStartString
        timeStartHistory.text = Common.formatTime(timeStart)
        val speedStartString = Common.formatSpeed(speedStart) + getString(R.string.unit_speed)
        speedStartHistory.text = speedStartString

        val walkDistCPString = "%.0f".format(walkDistCP) + getString(R.string.unit_dist)
        walkDistCPHistory.text = walkDistCPString
        val flyDistCPString = "%.0f".format(flyDistCP) + getString(R.string.unit_dist)
        flyDistCPHistory.text = flyDistCPString
        timeCPHistory.text = Common.formatTime(timeCP)
        val speedCPString = Common.formatSpeed(speedCP) + getString(R.string.unit_speed)
        speedCPHistory.text = speedCPString

        val walkDistWPString = "%.0f".format(walkDistWP) + getString(R.string.unit_dist)
        walkDistWPHistory.text = walkDistWPString
        val flyDistWPString = "%.0f".format(flyDistWP) + getString(R.string.unit_dist)
        flyDistWPHistory.text = flyDistWPString
        timeWPHistory.text = Common.formatTime(timeWP)
        val speedWPString = Common.formatSpeed(speedWP) + getString(R.string.unit_speed)
        speedWPHistory.text = speedWPString
    }

    private fun drawTrack() {
        for (i in 0..locations.size - 2) {
            val first = LatLng(locations[i].latitude, locations[i].longitude)
            val second = LatLng(locations[i + 1].latitude, locations[i + 1].longitude)
            val distBetween = FloatArray(1)
            Location.distanceBetween(
                first.latitude, first.longitude,
                second.latitude, second.longitude, distBetween
            )
            val color = Common.getTrackColor(
                this,
                locations[i].recordedAt,
                locations[i + 1].recordedAt,
                distBetween[0]
            )
            mMap.addPolyline(PolylineOptions().add(first, second).color(color).width(C.TRACK_WIDTH))
        }
    }

    private fun drawMapMarkers() {
        if (locations.isNotEmpty()) {
            val markerStart = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_fiber_manual_record_black_18))
                .position(LatLng(locations.first().latitude, locations.first().longitude))
                .anchor(0.5f, 0.5f)
            mMap.addMarker(markerStart)
            val markerFinish = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_flag_black_36))
                .position(LatLng(locations.last().latitude, locations.last().longitude))
                .anchor(0.25f, 0.85f)
            mMap.addMarker(markerFinish)
        }
        for (cp in allCPs) {
            val markerCP = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_beenhere_black_36))
                .position(LatLng(cp.latitude, cp.longitude))
                .anchor(0.5f, 0.9f)
            mMap.addMarker(markerCP)
        }
        for (wp in allWPs) {
            val markerWP = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_36))
                .position(LatLng(wp.latitude, wp.longitude))
                .anchor(0.5f, 0.9f)
            mMap.addMarker(markerWP)
        }
    }

    private fun generateGfx(sessionLocalId: String, locations: List<LocationData>): String {
        val header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=" +
                "\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=" +
                "\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 " +
                "http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n"
        val name = "<name>$sessionLocalId</name><trkseg>\n"
        var segments = ""
        for (loc in locations) {
            segments += "<trkpt lat=\"" + loc.latitude.toString() +
                    "\" lon=\"" + loc.longitude.toString() +
                    "\"><time>" + Common.convertLongToDate(loc.recordedAt, DateFormat.GPX) +
                    "</time>" + "<type>" + loc.locationType + "</type>" +
                    "</trkpt>\n"
        }
        val footer = "</trkseg></trk></gpx>"

        return header + "\n" + name + "\n" + segments + "\n" + footer + "\n"
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonEditOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.edit_session_title_question)

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val input = EditText(this)
        params.setMargins(52, 24, 52, 24)
        input.layoutParams = params
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = getString(R.string.session_new_title_hint)
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton(getString(R.string.confirm)) { _, _ ->
            run {
                if (input.text.isNullOrBlank()) {
                    databaseConnector.setSessionName(sessionLocalId, getString(R.string.auto_session_name))
                } else {
                    databaseConnector.setSessionName(sessionLocalId, input.text.toString())
                }
                Common.showToastMsg(this, getString(R.string.session_name_changed))
            }
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            run {
                dialog.cancel()
            }
        }

        builder.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonDeleteOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.delete_session_question)

        builder.setPositiveButton(getString(R.string.confirm)) { _, _ ->
            run {
                databaseConnector.deleteAllLocationsWithSessionLocalId(sessionLocalId)
                databaseConnector.deleteAllSessionsWithLocalId(sessionLocalId)
                Common.showToastMsg(this, getString(R.string.session_deleted))
                finish()
            }
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            run {
                dialog.cancel()
            }
        }

        builder.show()
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonEmailOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        val fileContents = generateGfx(sessionLocalId, locations)
        val file = File.createTempFile(
            "session_$sessionLocalId",
            ".gpx",
            this.externalCacheDir
        )

        val fw = FileWriter(file)
        fw.write(fileContents)
        fw.flush()
        fw.close()

        val intent = Intent(Intent.ACTION_SEND)
        val email = if (State.userEmail != null) State.userEmail else ""
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_default_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_default_body))
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email_using)))
        } catch (ex: ActivityNotFoundException) {
            Common.showToastMsg(this, getString(R.string.no_email_clients_installed))
        }
    }
}
