package cz.motikator.rollerderbytimer

import android.content.Context
import android.content.SharedPreferences
import java.util.Date

class DataHelper(context: Context) {
    private var sharedPref: SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    private var jamTimerCounting = false
    private var startJamTime: Date? = null
    private var stopJamTime: Date? = null

    private var startPeriodTime: Date? = null
    private var jamNumber: Int = 1
    private var periodState: Int = STATE_PERIOD_1
    private var hasStartedAnyJam = false
    private var timeoutCounting = false
    private var startTimeoutTime: Date? = null
    private var timeoutType: String? = null
    private var stopJamDurationMs = 30 * 1000L
    private var pausePeriodDuringThirtySeconds = false

    init {
        jamTimerCounting = sharedPref.getBoolean(COUNTING_JAM_KEY, false)
        jamNumber = sharedPref.getInt(JAM_NUMBER_KEY, 1)
        periodState = sharedPref.getInt(PERIOD_STATE_KEY, STATE_PERIOD_1)
        hasStartedAnyJam = sharedPref.getBoolean(HAS_STARTED_ANY_JAM_KEY, false)
        timeoutCounting = sharedPref.getBoolean(COUNTING_TIMEOUT_KEY, false)
        timeoutType = sharedPref.getString(TIMEOUT_TYPE_KEY, null)
        stopJamDurationMs = sharedPref.getLong(STOP_JAM_DURATION_KEY, 30 * 1000L)
        pausePeriodDuringThirtySeconds = sharedPref.getBoolean(PAUSE_PERIOD_DURING_THIRTY_KEY, false)

        try {
            val startTimeoutMs = sharedPref.getLong(START_TIMEOUT_KEY, -1L)
            if (startTimeoutMs != -1L) {
                startTimeoutTime = Date(startTimeoutMs)
            }
        } catch (_: ClassCastException) {
            sharedPref.edit().remove(START_TIMEOUT_KEY).apply()
        }

        try {
            val startMs = sharedPref.getLong(START_JAM_KEY, -1L)
            if (startMs != -1L) {
                startJamTime = Date(startMs)
            }
        } catch (_: ClassCastException) {
            sharedPref.edit().remove(START_JAM_KEY).apply()
        }

        try {
            val stopMs = sharedPref.getLong(STOP_JAM_KEY, -1L)
            if (stopMs != -1L) {
                stopJamTime = Date(stopMs)
            }
        } catch (_: ClassCastException) {
            sharedPref.edit().remove(STOP_JAM_KEY).apply()
        }

        try {
            val startPeriodMs = sharedPref.getLong(START_PERIOD_KEY, -1L)
            if (startPeriodMs != -1L) {
                startPeriodTime = Date(startPeriodMs)
            }
        } catch (_: ClassCastException) {
            sharedPref.edit().remove(START_PERIOD_KEY).apply()
        }
    }

    fun startJamTime(): Date? = startJamTime

    fun setStartJamTime(date: Date?) {
        startJamTime = date
        with(sharedPref.edit()) {
            if (date == null) {
                remove(START_JAM_KEY)
            } else {
                putLong(START_JAM_KEY, date.time)
            }
            apply()
        }
    }

    fun stopJamTime(): Date? = stopJamTime

    fun setStopJamTime(date: Date?) {
        stopJamTime = date
        with(sharedPref.edit()) {
            if (date == null) {
                remove(STOP_JAM_KEY)
            } else {
                putLong(STOP_JAM_KEY, date.time)
            }
            apply()
        }
    }

    fun startPeriodTime(): Date? = startPeriodTime

    fun setStartPeriodTime(date: Date?) {
        startPeriodTime = date
        with(sharedPref.edit()) {
            if (date == null) {
                remove(START_PERIOD_KEY)
            } else {
                putLong(START_PERIOD_KEY, date.time)
            }
            apply()
        }
    }

    fun jamNumber(): Int = jamNumber

    fun setJamNumber(number: Int) {
        jamNumber = number
        with(sharedPref.edit()) {
            putInt(JAM_NUMBER_KEY, number)
            apply()
        }
    }

    fun periodState(): Int = periodState

    fun setPeriodState(state: Int) {
        periodState = state
        with(sharedPref.edit()) {
            putInt(PERIOD_STATE_KEY, state)
            apply()
        }
    }

    fun jamTimerCounting(): Boolean = jamTimerCounting

    fun setJamTimerCounting(value: Boolean) {
        jamTimerCounting = value
        with(sharedPref.edit()) {
            putBoolean(COUNTING_JAM_KEY, value)
            apply()
        }
    }

    fun hasStartedAnyJam(): Boolean = hasStartedAnyJam

    fun setHasStartedAnyJam(value: Boolean) {
        hasStartedAnyJam = value
        with(sharedPref.edit()) {
            putBoolean(HAS_STARTED_ANY_JAM_KEY, value)
            apply()
        }
    }

    fun timeoutCounting(): Boolean = timeoutCounting

    fun setTimeoutCounting(value: Boolean) {
        timeoutCounting = value
        with(sharedPref.edit()) {
            putBoolean(COUNTING_TIMEOUT_KEY, value)
            apply()
        }
    }

    fun startTimeoutTime(): Date? = startTimeoutTime

    fun setStartTimeoutTime(date: Date?) {
        startTimeoutTime = date
        with(sharedPref.edit()) {
            if (date == null) {
                remove(START_TIMEOUT_KEY)
            } else {
                putLong(START_TIMEOUT_KEY, date.time)
            }
            apply()
        }
    }

    fun stopJamDurationMs(): Long = stopJamDurationMs

    fun setStopJamDurationMs(durationMs: Long) {
        stopJamDurationMs = durationMs
        with(sharedPref.edit()) {
            putLong(STOP_JAM_DURATION_KEY, durationMs)
            apply()
        }
    }

    fun timeoutType(): String? = timeoutType

    fun setTimeoutType(type: String?) {
        timeoutType = type
        with(sharedPref.edit()) {
            if (type == null) {
                remove(TIMEOUT_TYPE_KEY)
            } else {
                putString(TIMEOUT_TYPE_KEY, type)
            }
            apply()
        }
    }

    fun pausePeriodDuringThirtySeconds(): Boolean = pausePeriodDuringThirtySeconds

    fun setPausePeriodDuringThirtySeconds(value: Boolean) {
        pausePeriodDuringThirtySeconds = value
        with(sharedPref.edit()) {
            putBoolean(PAUSE_PERIOD_DURING_THIRTY_KEY, value)
            apply()
        }
    }

    companion object {
        const val PREFERENCES = "prefs"
        const val START_JAM_KEY = "startJamKey"
        const val STOP_JAM_KEY = "endJamKey"
        const val COUNTING_JAM_KEY = "countingJamKey"

        const val START_PERIOD_KEY = "startPeriodKey"
        const val JAM_NUMBER_KEY = "jamNumberKey"
        const val PERIOD_STATE_KEY = "periodStateKey"
        const val HAS_STARTED_ANY_JAM_KEY = "hasStartedAnyJamKey"
        const val START_TIMEOUT_KEY = "startTimeoutKey"
        const val COUNTING_TIMEOUT_KEY = "countingTimeoutKey"
        const val TIMEOUT_TYPE_KEY = "timeoutTypeKey"
        const val STOP_JAM_DURATION_KEY = "stopJamDurationKey"
        const val PAUSE_PERIOD_DURING_THIRTY_KEY = "pausePeriodDuringThirtyKey"

        const val STATE_PERIOD_1 = 1
        const val STATE_HALFTIME = 2
        const val STATE_PERIOD_2 = 3
    }
}
