package ee.iti0213.navigationhelper

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ee.iti0213.navigationhelper.db.LocationData
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.helper.Common
import kotlinx.android.synthetic.main.cp_stats_history.*
import kotlinx.android.synthetic.main.start_stats_history.*
import kotlinx.android.synthetic.main.wp_stats_history.*

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

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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

    fun buttonEditOnClick(view: View) {
        //TODO: Edit session name in DB
    }

    fun buttonDeleteOnClick(view: View) {
        //TODO: Delete session from DB and finish activity
    }

    fun buttonEmailOnClick(view: View) {
        //TODO: Send session GPX to user email
    }
}
