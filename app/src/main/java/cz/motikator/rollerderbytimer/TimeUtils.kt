package cz.motikator.rollerderbytimer

import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import java.util.Locale

object TimeUtils {

    fun smallDecimalSpannable(timeStr: String): CharSequence {
        val dotIndex = timeStr.indexOf('.')
        if (dotIndex != -1 && dotIndex < timeStr.length) {
            val spannable = SpannableString(timeStr)
            spannable.setSpan(
                RelativeSizeSpan(0.6f),
                dotIndex,
                timeStr.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }
        return timeStr
    }

    fun thirtySecondsStringFromLong(ms: Long): String {
        val adjustedMs = if (ms > 0) ms + 99L else ms
        val absMs = if (adjustedMs < 0) -adjustedMs else adjustedMs
        val totalSeconds = absMs / 1000
        val remainder = absMs % 1000
        val tenths = remainder / 100

        val formatted = String.format(Locale.getDefault(), "%02d.%d", totalSeconds, tenths)
        return if (adjustedMs < 0) "-$formatted" else formatted
    }

    fun jamTimeStringFromLong(ms: Long, allowNegative: Boolean = false): String {
        if (!allowNegative && ms < 0) {
            return "00:00.0"
        }
        val adjustedMs = if (ms > 0) ms + 99L else ms
        val absMs = if (adjustedMs < 0) -adjustedMs else adjustedMs
        val totalSeconds = absMs / 1000
        val remainder = absMs % 1000
        val tenths = remainder / 100
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60

        val formatted = String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
        return if (adjustedMs < 0) "-$formatted" else formatted
    }

    fun elapsedTimeStringFromLong(ms: Long): String {
        val absMs = if (ms < 0) 0L else ms
        val totalSeconds = absMs / 1000
        val remainder = absMs % 1000
        val tenths = remainder / 100
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        return String.format(Locale.getDefault(), "%02d:%02d.%d", minutes, seconds, tenths)
    }

    fun timeStringFromLong(ms: Long, allowNegative: Boolean = false): String {
        if (!allowNegative && ms < 0) {
            return "00:00"
        }
        if (ms >= 0) {
            val totalSeconds = (ms + 999) / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        } else {
            val absMs = -ms
            val totalSeconds = absMs / 1000
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60
            return String.format(Locale.getDefault(), "-%02d:%02d", minutes, seconds)
        }
    }
}
