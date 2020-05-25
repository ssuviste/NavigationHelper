package ee.iti0213.navigationhelper.api

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import java.util.*

class API {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName

        private const val BASE_URL = "https://sportmap.akaver.com/api/v1"

        const val AUTH_REGISTER = "/Account/Register"
        const val AUTH_LOGIN = "/Account/Login"
        const val GPS_SESSION = "/GpsSessions"
        const val GPS_LOCATIONS = "/GpsLocations"

        const val REST_ID_LOC = "00000000-0000-0000-0000-000000000001"
        const val REST_ID_WP = "00000000-0000-0000-0000-000000000002"
        const val REST_ID_CP = "00000000-0000-0000-0000-000000000003"

        var token: String? = null
        var currentSessionId: String? = null

        fun postToUrl(context: Context, endpoint: String, reqParams: JSONObject, useToken: Boolean,
                      callBack: (context: Context, resp: JSONObject) -> Unit,
                      errorCallBack: (context: Context, VolleyError) -> Unit) {

            val handler = WebAPISingletonHandler.getInstance(context)
            val httpRequest = object : JsonObjectRequest(
                Method.POST,
                BASE_URL + endpoint,
                reqParams,
                Response.Listener { response ->
                    Log.d(TAG, response.toString())
                    callBack(context, response)
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, error.toString())
                    errorCallBack(context, error)
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    for ((key, value) in super.getHeaders()) {
                        headers[key] = value
                    }
                    if (useToken && token != null) {
                        headers["Authorization"] = "Bearer $token"
                    }
                    return headers
                }
            }
            handler.addToRequestQueue(httpRequest)
        }
    }
}