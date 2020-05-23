package ee.iti0213.navigationhelper.api

import android.content.Context
import com.android.volley.VolleyError
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.helper.State
import org.json.JSONObject
import java.nio.charset.Charset

class APICallback {
    companion object {
        fun register(context: Context, response: JSONObject) {
            API.token = response.getString("token")
            State.loggedIn = true
            Common.showToastMsg(context, response.getString("status"))
        }

        fun registerError(context: Context, error: VolleyError) {
            State.userEmail = null
            State.loggedIn = false
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Common.showToastMsg(context, message)
        }

        fun login(context: Context, response: JSONObject) {
            API.token = response.getString("token")
            State.loggedIn = true
            Common.showToastMsg(context, response.getString("status"))
        }

        fun loginError(context: Context, error: VolleyError) {
            State.userEmail = null
            State.loggedIn = false
            val message = String(error.networkResponse.data, Charset.defaultCharset())
                .replace("\"", " ")
                .replace("{ messages :[ ", "")
                .replace(" ]}", "")
            Common.showToastMsg(context, message)
        }
    }
}