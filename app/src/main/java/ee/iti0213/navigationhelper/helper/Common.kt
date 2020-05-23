package ee.iti0213.navigationhelper.helper

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.muddzdev.styleabletoast.StyleableToast
import ee.iti0213.navigationhelper.R
import java.util.regex.Pattern

class Common {
    companion object {
        fun showToastMsg(context: Context, msg: String) {
            StyleableToast.makeText(context, msg, Toast.LENGTH_SHORT, R.style.glassToast).show()
        }

        fun isNameValid(context: Context, target: CharSequence?): Boolean {
            if (target.isNullOrBlank()) {
                showToastMsg(context, context.getString(R.string.invalid_name))
                return false
            }
            return true
        }

        fun isEmailValid(context: Context, target: CharSequence?): Boolean {
            if (target.isNullOrBlank()) {
                showToastMsg(context, context.getString(R.string.invalid_email_empty))
                return false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
                showToastMsg(context, context.getString(R.string.invalid_email))
                return false
            }
            return true
        }

        fun isPasswordValid(context: Context, target: CharSequence?): Boolean {
            if (target.isNullOrEmpty()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_empty))
                return false
            } else if (target.length < 6) {
                showToastMsg(context, context.getString(R.string.invalid_pw_length))
                return false
            } else if (Pattern.compile("^.*\\s.*$").matcher(target).matches()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_ws))
                return false
            } else if (!Pattern.compile("^.*[a-z].*$").matcher(target).matches()
                || !Pattern.compile("^.*[A-Z].*$").matcher(target).matches()
            ) {
                showToastMsg(context, context.getString(R.string.invalid_pw_uc_lc))
                return false
            } else if (!Pattern.compile("^.*\\d.*\$").matcher(target).matches()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_number))
                return false
            } else if (!Pattern.compile("^.*[^a-zA-Z0-9].*\$").matcher(target).matches()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_special_char))
                return false
            }
            return true
        }

        fun isLoginPasswordValid(context: Context, target: CharSequence?): Boolean {
            if (target.isNullOrEmpty()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_empty))
                return false
            } else if (target.length < 6) {
                showToastMsg(context, context.getString(R.string.invalid_pw_length))
                return false
            }
            return true
        }

        fun getTrackColor(context: Context, first: Location, second: Location): Int {
            return if (Preferences.gradientEnabled) {
                val pace = (second.time - first.time) / 60f / first.distanceTo(second)
                val ratio = when {
                    pace < Preferences.gradientMinPace -> 0f
                    pace > Preferences.gradientMaxPace -> 1f
                    else -> (pace - Preferences.gradientMinPace) /
                            (Preferences.gradientMaxPace - Preferences.gradientMinPace)
                }
                var red = if (ratio < 0.5) (255 * 2 * ratio).toInt().toString(16) else "ff"
                var green = if (ratio > 0.5) (255 * 2 * (1 - ratio)).toInt().toString(16) else "ff"
                if (red.length < 2) red = "0$red"
                if (green.length < 2) green = "0$green"
                Color.parseColor("#${red}${green}00")
            } else {
                ContextCompat.getColor(context, R.color.colorBlueGoogle)
            }
        }
    }
}