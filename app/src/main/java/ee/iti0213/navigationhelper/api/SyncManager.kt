package ee.iti0213.navigationhelper.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.helper.DateFormat
import ee.iti0213.navigationhelper.helper.Preferences
import ee.iti0213.navigationhelper.helper.State
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
            reqParams.put("recordedAt", Common.convertLongToDate(session.startTime, DateFormat.SERVER))
            reqParams.put("paceMin", session.paceMin * 60)
            reqParams.put("paceMax", session.paceMax * 60)
            API.postToUrl(
                context!!, API.GPS_SESSION, reqParams, true,
                { c, r ->  APICallback.sessionInit(c, r) },
                { c, r ->  APICallback.sessionInitError(c, r) }
            )
        }
    }

    private fun syncLocations() {

    }

    fun writeSessionServerIdToDatabase(startTime: Long, serverId: String) {
        databaseConnector!!.setSessionServerId(startTime, serverId)
    }
}