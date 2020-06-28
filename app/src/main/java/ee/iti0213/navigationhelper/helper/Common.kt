package ee.iti0213.navigationhelper.helper

import android.content.Context
import android.graphics.Color
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.muddzdev.styleabletoast.StyleableToast
import ee.iti0213.navigationhelper.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

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

        fun isPasswordConfirmed(context: Context, t1: CharSequence?, t2: CharSequence?): Boolean {
            if (t1.toString() != t2.toString()) {
                showToastMsg(context, context.getString(R.string.invalid_pw_confirmation))
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

        fun getTrackColor(context: Context, firstTime: Long, secondTime: Long, dist: Float): Int {
            val pref = context.getSharedPreferences(C.PREF_FILE_KEY, Context.MODE_PRIVATE)
            val gradientEnabled = pref.getBoolean(C.PREF_GRAD_ENABLED_KEY, C.DEFAULT_GRAD_ENABLED)
            return if (gradientEnabled) {
                val gradientMinPace = pref.getInt(C.PREF_GRAD_MIN_PACE_KEY, C.DEFAULT_GRAD_MIN_PACE)
                val gradientMaxPace = pref.getInt(C.PREF_GRAD_MAX_PACE_KEY, C.DEFAULT_GRAD_MAX_PACE)
                val pace = (secondTime - firstTime) / 60f / dist
                val ratio = when {
                    pace < gradientMinPace -> 0f
                    pace > gradientMaxPace -> 1f
                    else -> (pace - gradientMinPace) / (gradientMaxPace - gradientMinPace)
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

        fun generateHashString(): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..C.SESSION_LOCAL_ID_LENGTH)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }

        fun convertLongToDate(time: Long, format: DateFormat): String {
            val sdf = when (format) {
                DateFormat.DEFAULT ->
                    SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                DateFormat.SERVER ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                DateFormat.GPX ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            }
            return sdf.format(Date(time))
        }

        fun convertDateToLong(date: String, format: DateFormat): Long {
            val sdf = when (format) {
                DateFormat.DEFAULT ->
                    SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                DateFormat.SERVER ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                DateFormat.GPX ->
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            }
            return sdf.parse(date)!!.time
        }

        fun formatTime(time: Long): String {
            val hours = time / 3600
            if (hours > 99) return "99:59:59"
            val minutes = (time % 3600) / 60
            val seconds = time % 60
            val hoursString = when {
                hours < 10 -> "0$hours"
                else -> "$hours"
            }
            val minutesString = when {
                minutes < 10 -> ":0$minutes"
                else -> ":$minutes"
            }
            val secondsString = when {
                seconds < 10 -> ":0$seconds"
                else -> ":$seconds"
            }
            return "$hoursString$minutesString$secondsString"
        }

        fun formatSpeed(time: Long): String {
            val minutes = time / 60
            if (minutes > 99) return "99:59"
            val seconds = time % 60
            val minutesString = when {
                minutes < 10 -> "0$minutes"
                else -> "$minutes"
            }
            val secondsString = when {
                seconds < 10 -> ":0$seconds"
                else -> ":$seconds"
            }
            return "$minutesString$secondsString"
        }
    }
}