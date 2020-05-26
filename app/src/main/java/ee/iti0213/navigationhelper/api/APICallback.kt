package ee.iti0213.navigationhelper.api

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.helper.DateFormat
import ee.iti0213.navigationhelper.helper.State
import org.json.JSONObject
import java.nio.charset.Charset

class APICallback {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        fun registerSuccess(context: Context, response: JSONObject) {
            API.token = response.getString("token")
            State.loggedIn = true
            Common.showToastMsg(context, response.getString("status"))
        }

        fun registerFail(context: Context, error: VolleyError) {
            State.userEmail = null
            State.loggedIn = false
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Common.showToastMsg(context, message)
        }

        fun loginSuccess(context: Context, response: JSONObject) {
            API.token = response.getString("token")
            State.loggedIn = true
            Common.showToastMsg(context, response.getString("status"))
        }

        fun loginFail(context: Context, error: VolleyError) {
            State.userEmail = null
            State.loggedIn = false
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Common.showToastMsg(context, message)
        }

        @Suppress("UNUSED_PARAMETER")
        fun sessionSyncSuccess(context: Context, response: JSONObject) {
            Log.d(TAG, "Session sync success")
            val startTime = Common.convertDateToLong(response.getString("recordedAt"), DateFormat.SERVER)
            val serverId = response.getString("id")
            SyncManager.updateSessionServerId(startTime, serverId)
        }

        @Suppress("UNUSED_PARAMETER")
        fun sessionSyncFail(context: Context, error: VolleyError) {
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Log.e(TAG, message)
        }

        @Suppress("UNUSED_PARAMETER")
        fun locationSyncSuccess(context: Context, response: JSONObject) {
            Log.d(TAG, "Location sync success")
            val recordedAt = Common.convertDateToLong(response.getString("recordedAt"), DateFormat.SERVER)
            SyncManager.setLocationSyncNeedToZero(recordedAt)
        }

        @Suppress("UNUSED_PARAMETER")
        fun locationSyncFail(context: Context, error: VolleyError) {
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Log.e(TAG, message)
        }
    }
}