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
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (State.loggedIn) {
            finish()
        }
        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.register)
        toolbar.logo = getDrawable(R.drawable.baseline_person_add_24)
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
    fun buttonRegisterOnClick(view: View) {
        if (SystemClock.elapsedRealtime() - State.toastBtnLastClickTime < C.TOASTED_BTN_COOL_DOWN) {
            return
        }
        State.toastBtnLastClickTime = SystemClock.elapsedRealtime()

        if (Common.isNameValid(this, editTextFirstName.text)
            && Common.isNameValid(this, editTextLastName.text)
            && Common.isEmailValid(this, editTextEmail.text)
            && Common.isPasswordValid(this, editTextPassword.text)
            && Common.isPasswordConfirmed(this, editTextPassword.text, editTextConfirmPassword.text)
        ) {

            State.userEmail = editTextEmail.text.toString()
            val reqParams = JSONObject()
            reqParams.put("email", editTextEmail.text.toString())
            reqParams.put("password", editTextPassword.text.toString())
            reqParams.put("firstName", editTextFirstName.text.toString())
            reqParams.put("lastName", editTextLastName.text.toString())
            API.postToUrl(
                this, API.AUTH_REGISTER, reqParams, false,
                { c, r -> APICallback.registerSuccess(c, r) },
                { c, r -> APICallback.registerFail(c, r) }
            )
            finish()
        }
    }
}
