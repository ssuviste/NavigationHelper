package ee.iti0213.navigationhelper.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class WebAPISingletonHandler(context: Context) {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
        private var instance: WebAPISingletonHandler? = null
        private var mContext: Context? = null

        @Synchronized
        fun getInstance(context: Context): WebAPISingletonHandler {
            if (instance == null) {
                instance = WebAPISingletonHandler(context)
            }
            return instance!!
        }
    }

    init {
        mContext = context
    }

    private var requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                field = Volley.newRequestQueue(mContext)
            }
            return field
        }

    fun <T> addToRequestQueue(request: Request<T>) {
        Log.d(TAG, request.url)
        request.tag = TAG
        requestQueue?.add(request)
    }

    @Suppress("UNUSED")
    fun cancelPendingRequest(tag: String) {
        if (requestQueue != null) {
            requestQueue!!.cancelAll(if (tag.isBlank()) TAG else tag)
        }
    }
}