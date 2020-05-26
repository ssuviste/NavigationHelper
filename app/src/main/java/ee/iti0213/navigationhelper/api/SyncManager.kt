package ee.iti0213.navigationhelper.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.helper.*
import org.json.JSONObject

object SyncManager {
    private var context: Context? = null
    private var databaseConnector: Repository? = null

    private var handler: Handler = Handler(Looper.getMainLooper())
    private val syncToServer = object : Runnable {
        override fun run() {
            if (Preferences.syncEnabled && State.loggedIn && API.token != null) {
                syncSessions()
                syncLocations()
            }
            handler.postDelayed(
                this,
                Preferences.syncInterval
            )
        }
    }

    fun initialize(context: Context) {
        if (this.context == null || databaseConnector == null) {
            this.context = context
            databaseConnector = Repository(context).open()
            handler.postDelayed(
                syncToServer,
                Preferences.syncInterval
            )
        }
    }

    private fun syncSessions() {
        val sessionsToSync = databaseConnector!!.getAllSessionsWhereServerIdNull()
        for (session in sessionsToSync) {
            val reqParams = JSONObject()
            reqParams.put("name", session.sessionName)
            reqParams.put("description", session.description)
            reqParams.put(
                "recordedAt",
                Common.convertLongToDate(session.startTime, DateFormat.SERVER)
            )
            reqParams.put("paceMin", session.paceMin * 60)
            reqParams.put("paceMax", session.paceMax * 60)
            API.postToUrl(
                context!!, API.GPS_SESSION, reqParams, true,
                { c, r -> APICallback.sessionSyncSuccess(c, r) },
                { c, r -> APICallback.sessionSyncFail(c, r) }
            )
        }
    }

    private fun syncLocations() {
        val locationsToSync = databaseConnector!!.getAllLocationsWhichNeedSync()
        for (loc in locationsToSync) {
            val sessionServerId =
                databaseConnector!!.getSessionServerIdWhereLocalId(loc.sessionLocalId)
            if (!sessionServerId.isNullOrBlank() && sessionServerId != C.LOCAL_SESSION) {
                val locationType = when (loc.locationType) {
                    C.LOC_TYPE_LOC -> API.REST_ID_LOC
                    C.LOC_TYPE_CP -> API.REST_ID_CP
                    else -> API.REST_ID_WP
                }
                val recordedAt = Common.convertLongToDate(loc.recordedAt, DateFormat.SERVER)
                val reqParams = JSONObject()
                reqParams.put("recordedAt", recordedAt)
                reqParams.put("latitude", loc.latitude)
                reqParams.put("longitude", loc.longitude)
                reqParams.put("accuracy", loc.accuracy)
                reqParams.put("altitude", loc.altitude)
                reqParams.put("gpsSessionId", sessionServerId)
                reqParams.put("gpsLocationTypeId", locationType)
                API.postToUrl(
                    context!!, API.GPS_LOCATIONS, reqParams, true,
                    { c, r -> APICallback.locationSyncSuccess(c, r) },
                    { c, r -> APICallback.locationSyncFail(c, r) }
                )
            }
        }
    }

    fun updateSessionServerId(startTime: Long, serverId: String) {
        databaseConnector!!.setSessionServerId(startTime, serverId)
    }

    fun setLocationSyncNeedToZero(startTime: Long) {
        databaseConnector!!.setLocationSyncNeed(startTime, 0)
    }
}