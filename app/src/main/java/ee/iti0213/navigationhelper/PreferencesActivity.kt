package ee.iti0213.navigationhelper

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import ee.iti0213.navigationhelper.helper.C
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

        val pref = this.getSharedPreferences(C.PREF_FILE_KEY, Context.MODE_PRIVATE)
        val prefEditor = pref.edit()

        switchSyncEnable.isChecked =
            pref.getBoolean(C.PREF_SYNC_ENABLED_KEY, C.DEFAULT_SYNC_ENABLED)
        switchSyncEnable.setOnCheckedChangeListener { _, isChecked ->
            prefEditor.putBoolean(C.PREF_SYNC_ENABLED_KEY, isChecked)
            prefEditor.apply()
        }

        switchGradientEnable.isChecked =
            pref.getBoolean(C.PREF_GRAD_ENABLED_KEY, C.DEFAULT_GRAD_ENABLED)
        switchGradientEnable.setOnCheckedChangeListener { _, isChecked ->
            prefEditor.putBoolean(C.PREF_GRAD_ENABLED_KEY, isChecked)
            prefEditor.apply()
        }

        val currSyncInterval = pref.getLong(C.PREF_SYNC_INTERVAL_KEY, C.DEFAULT_SYNC_INTERVAL)
        textViewSyncInterval.text = (currSyncInterval.toInt() / 1000).toString()
        seekBarSyncInterval.progress =
            (currSyncInterval - C.MIN_SYNC_INTERVAL_IN_MILLISECONDS).toInt() / 1000
        seekBarSyncInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefEditor.putLong(
                    C.PREF_SYNC_INTERVAL_KEY,
                    C.MIN_SYNC_INTERVAL_IN_MILLISECONDS + progress.toLong() * 1000
                )
                prefEditor.apply()
                textViewSyncInterval.text =
                    (C.MIN_SYNC_INTERVAL_IN_MILLISECONDS.toInt() / 1000 + progress).toString()
            }
        })

        val currGpsAccuracy = pref.getInt(C.PREF_GPS_ACC_KEY, C.DEFAULT_GPS_ACC)
        textViewGpsAccuracy.text = currGpsAccuracy.toString()
        seekBarGpsAccuracy.progress = currGpsAccuracy - C.LOC_MIN_ACCURACY
        seekBarGpsAccuracy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefEditor.putInt(C.PREF_GPS_ACC_KEY, C.LOC_MIN_ACCURACY + progress)
                prefEditor.apply()
                textViewGpsAccuracy.text = (C.LOC_MIN_ACCURACY + progress).toString()
            }
        })

        val currMinPace = pref.getInt(C.PREF_GRAD_MIN_PACE_KEY, C.DEFAULT_GRAD_MIN_PACE)
        val currMaxPace = pref.getInt(C.PREF_GRAD_MAX_PACE_KEY, C.DEFAULT_GRAD_MAX_PACE)
        textViewMinPace.text = currMinPace.toString()
        textViewMaxPace.text = currMaxPace.toString()
        rangeBarGradientPace.start = currMinPace - C.PACE_MIN
        rangeBarGradientPace.end = currMaxPace - C.PACE_MIN
        rangeBarGradientPace.onTrackRangeListener = object : OnTrackRangeListener {
            override fun onStartRangeChanged(@NotNull rangeView: SimpleRangeView, start: Int) {
                prefEditor.putInt(C.PREF_GRAD_MIN_PACE_KEY, start + C.PACE_MIN)
                prefEditor.apply()
                textViewMinPace.text = (start + C.PACE_MIN).toString()
            }

            override fun onEndRangeChanged(@NotNull rangeView: SimpleRangeView, end: Int) {
                prefEditor.putInt(C.PREF_GRAD_MAX_PACE_KEY, end + C.PACE_MIN)
                prefEditor.apply()
                textViewMaxPace.text = (end + C.PACE_MIN).toString()
            }
        }
    }
}
