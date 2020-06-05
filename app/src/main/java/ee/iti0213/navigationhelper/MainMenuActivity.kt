package ee.iti0213.navigationhelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import ee.iti0213.navigationhelper.api.API
import ee.iti0213.navigationhelper.api.SyncManager
import ee.iti0213.navigationhelper.state.State
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {
    companion object {
        private val TAG = this::class.java.declaringClass!!.simpleName
    }

    private var loginMenu: Menu? = null
    private var loggedIn = false

    private val minUIUpdateInterval = 1000L
    private val maxUIUpdateInterval = 2000L

    private lateinit var handler: Handler
    private val checkForLoginUpdates = object : Runnable {
        override fun run() {
            if (loggedIn != State.loggedIn) {
                updateLoginUI()
            }
            handler.postDelayed(this, maxUIUpdateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        SyncManager.initialize(this)

        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.app_name)
        toolbar.logo = getDrawable(R.drawable.baseline_explore_24)
        toolbar.overflowIcon = getDrawable(R.drawable.baseline_account_box_24)
        @Suppress("DEPRECATION")
        toolbar.logo.setColorFilter(white, mode)
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        updateButtonToMapsActivityState()
        updateButtonToPreferencesActivityState()
        updateLoginMenu()
        updateUserTextViews()

        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(checkForLoginUpdates, 0L)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        handler.removeCallbacks(checkForLoginUpdates)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.user_menu, menu)
        loginMenu = menu!!
        updateLoginMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        when (item.itemId) {
            R.id.buttonLogout -> {
                handler.removeCallbacks(checkForLoginUpdates)
                API.token = null
                State.loggedIn = !State.loggedIn
                State.userEmail = null
                handler.postDelayed(checkForLoginUpdates, minUIUpdateInterval)
            }
            R.id.buttonToLoginActivity -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.buttonToRegisterActivity -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateButtonToMapsActivityState() {
        if (State.sessionActive) {
            buttonToMapsActivity.text = getString(R.string.current_session)
        } else {
            buttonToMapsActivity.text = getString(R.string.new_session)
        }
    }

    private fun updateButtonToPreferencesActivityState() {
        if (State.sessionActive) {
            buttonToPreferencesActivity.isEnabled = false
            buttonToPreferencesActivity.setTextColor(ContextCompat.getColor(this, R.color.colorGray))
            if (resources.getBoolean(R.bool.isTablet)) {
                buttonToPreferencesActivity.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.baseline_settings_gray_36),
                    null,
                    null,
                    null
                )
            } else {
                buttonToPreferencesActivity.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.baseline_settings_gray_24),
                    null,
                    null,
                    null
                )
            }
        } else {
            buttonToPreferencesActivity.isEnabled = true
            buttonToPreferencesActivity.setTextColor(ContextCompat.getColor(this, R.color.colorGreenMilitary))
            if (resources.getBoolean(R.bool.isTablet)) {
                buttonToPreferencesActivity.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.baseline_settings_green_36),
                    null,
                    null,
                    null
                )
            } else {
                buttonToPreferencesActivity.setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.baseline_settings_green_24),
                    null,
                    null,
                    null
                )
            }
        }
    }

    private fun updateLoginUI() {
        loggedIn = State.loggedIn
        updateUserTextViews()
        updateLoginMenu()
    }

    private fun updateLoginMenu() {
        if (loginMenu == null) {
            return
        }
        when {
            State.sessionActive -> {
                loginMenu!!.findItem(R.id.buttonLogout).isVisible = false
                loginMenu!!.findItem(R.id.buttonToLoginActivity).isVisible = false
                loginMenu!!.findItem(R.id.buttonToRegisterActivity).isVisible = false
            }
            loggedIn -> {
                loginMenu!!.findItem(R.id.buttonLogout).isVisible = true
                loginMenu!!.findItem(R.id.buttonToLoginActivity).isVisible = false
                loginMenu!!.findItem(R.id.buttonToRegisterActivity).isVisible = false
            }
            else -> {
                loginMenu!!.findItem(R.id.buttonLogout).isVisible = false
                loginMenu!!.findItem(R.id.buttonToLoginActivity).isVisible = true
                loginMenu!!.findItem(R.id.buttonToRegisterActivity).isVisible = true
            }
        }
    }

    private fun updateUserTextViews() {
        if (loggedIn) {
            textViewCurrentUser.text = getString(R.string.current_user)
            if (State.userEmail != null) {
                textViewEmail.text = State.userEmail
            }
            textViewEmail.visibility = View.VISIBLE
        } else {
            textViewCurrentUser.text = getString(R.string.logged_out)
            textViewEmail.visibility = View.GONE
            textViewEmail.text = getString(R.string.email_unknown)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonToMapsActivityOnClick(view: View) {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonToHistoryActivityOnClick(view: View) {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonToPreferencesActivityOnClick(view: View) {
        startActivity(Intent(this, PreferencesActivity::class.java))
    }

    @Suppress("UNUSED_PARAMETER")
    fun buttonToHelpActivityOnClick(view: View) {
        startActivity(Intent(this, HelpActivity::class.java))
    }
}
