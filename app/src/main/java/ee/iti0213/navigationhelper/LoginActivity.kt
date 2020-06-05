package ee.iti0213.navigationhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import ee.iti0213.navigationhelper.api.API
import ee.iti0213.navigationhelper.api.APICallback
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.helper.Common
import ee.iti0213.navigationhelper.state.State
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (State.loggedIn) {
            finish()
        }
        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.login)
        toolbar.logo = getDrawable(R.drawable.baseline_person_24)
        @Suppress("DEPRECATION")
        toolbar.logo.setColorFilter(white, mode)
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        super.onResume()
        if (State.loggedIn) {
            finish()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonLoginOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        if (Common.isEmailValid(this, editTextLoginEmail.text)
            && Common.isLoginPasswordValid(this, editTextLoginPassword.text)) {
            State.userEmail = editTextLoginEmail.text.toString()
            val reqParams = JSONObject()
            reqParams.put("email", editTextLoginEmail.text.toString())
            reqParams.put("password", editTextLoginPassword.text.toString())
            API.postToUrl(
                this, API.AUTH_LOGIN, reqParams, false,
                { c, r ->  APICallback.loginSuccess(c, r) },
                { c, r ->  APICallback.loginFail(c, r)}
            )
            finish()
        }
    }
}
