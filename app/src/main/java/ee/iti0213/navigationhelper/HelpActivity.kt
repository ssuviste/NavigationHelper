package ee.iti0213.navigationhelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.help)
        toolbar.logo = getDrawable(R.drawable.baseline_help_24)
        //toolbar.overflowIcon = getDrawable(R.drawable.baseline_account_box_24)
        @Suppress("DEPRECATION")
        toolbar.logo.setColorFilter(white, mode)
        setSupportActionBar(toolbar)
    }
}
