package ee.iti0213.navigationhelper

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import ee.iti0213.navigationhelper.helper.C
import ee.iti0213.navigationhelper.state.Preferences
import kotlinx.android.synthetic.main.activity_preferences.*
import me.bendik.simplerangeview.SimpleRangeView
import me.bendik.simplerangeview.SimpleRangeView.OnTrackRangeListener
import org.jetbrains.annotations.NotNull


class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        val white = ContextCompat.getColor(this, R.color.colorWhite)
        val mode = android.graphics.PorterDuff.Mode.MULTIPLY
        val toolbar = findViewById<Toolbar>(R.id.mainMenuToolbar)
        toolbar.title = getString(R.string.preferences)
        toolbar.logo = getDrawable(R.drawable.baseline_settings_24)
        @Suppress("DEPRECATION")
        toolbar.logo.setColorFilter(white, mode)
        setSupportActionBar(toolbar)

        switchSyncEnable.isChecked = Preferences.syncEnabled
        switchSyncEnable.setOnCheckedChangeListener { _, isChecked ->
            Preferences.syncEnabled = isChecked
        }

        switchGradientEnable.isChecked = Preferences.gradientEnabled
        switchGradientEnable.setOnCheckedChangeListener { _, isChecked ->
            Preferences.gradientEnabled = isChecked
        }

        textViewSyncInterval.text = (Preferences.syncInterval.toInt() / 1000).toString()
        seekBarSyncInterval.progress =
            (Preferences.syncInterval - C.MIN_SYNC_INTERVAL_IN_MILLISECONDS).toInt() / 1000
        seekBarSyncInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Preferences.syncInterval =
                    C.MIN_SYNC_INTERVAL_IN_MILLISECONDS + progress.toLong() * 1000
                textViewSyncInterval.text = (Preferences.syncInterval.toInt() / 1000).toString()
            }
        })

        textViewGpsAccuracy.text = Preferences.gpsAccuracy.toString()
        seekBarGpsAccuracy.progress = Preferences.gpsAccuracy - C.LOC_MIN_ACCURACY
        seekBarGpsAccuracy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Preferences.gpsAccuracy = C.LOC_MIN_ACCURACY + progress
                textViewGpsAccuracy.text = Preferences.gpsAccuracy.toString()
            }
        })

        textViewMinPace.text = Preferences.gradientMinPace.toString()
        textViewMaxPace.text = Preferences.gradientMaxPace.toString()
        rangeBarGradientPace.start = Preferences.gradientMinPace - C.PACE_MIN
        rangeBarGradientPace.end = Preferences.gradientMaxPace - C.PACE_MIN
        rangeBarGradientPace.onTrackRangeListener = object : OnTrackRangeListener {
            override fun onStartRangeChanged(@NotNull rangeView: SimpleRangeView, start: Int) {
                Preferences.gradientMinPace = start + C.PACE_MIN
                textViewMinPace.text = (start + C.PACE_MIN).toString()
            }

            override fun onEndRangeChanged(@NotNull rangeView: SimpleRangeView, end: Int) {
                Preferences.gradientMaxPace = end + C.PACE_MIN
                textViewMaxPace.text = (end + C.PACE_MIN).toString()
            }
        }
    }
}
