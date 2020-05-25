package ee.iti0213.navigationhelper.api

import android.content.Context
import android.os.Handler
import android.os.Looper
import ee.iti0213.navigationhelper.db.Repository
import ee.iti0213.navigationhelper.helper.Preferences
import ee.iti0213.navigationhelper.helper.State

object SyncManager {
    private var repo: Repository? = null

    private var handler: Handler = Handler(Looper.getMainLooper())
    private val syncToServer = object : Runnable {
        override fun run() {
            syncSessions()
            syncLocations()

            if (repo != null) {
                println("hellohello")
            }

            handler.postDelayed(
                this,
                Preferences.syncInterval
            )
        }
    }

    fun initialize(context: Context) {
        if (repo == null) {
            repo = Repository(context)
            handler.postDelayed(
                syncToServer,
                Preferences.syncInterval
            )
        }
    }

    private fun syncSessions() {
        if (Preferences.syncEnabled && State.loggedIn && API.token != null) {

        }
    }

    private fun syncLocations() {

    }
}