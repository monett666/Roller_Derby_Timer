package cz.motikator.rollerderbytimer

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import cz.motikator.rollerderbytimer.databinding.ActivityMainBinding
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var dataHelper: DataHelper

    private val jamTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataHelper = DataHelper(applicationContext)

        binding.jamStartButton.setOnClickListener { startStopAction() }
        binding.resetButton.setOnClickListener { resetAction() }
        binding.goTo30.setOnClickListener { goTo30Action() }
        binding.goTo5.setOnClickListener { goTo5Action() }
        binding.teamTimeout.setOnClickListener { startTimeoutAction(getString(R.string.team_timeout)) }
        binding.officialTimeout.setOnClickListener { startTimeoutAction(getString(R.string.official_timeout)) }
        binding.emergencyStop.setOnClickListener { startTimeoutAction(getString(R.string.emergency_stop)) }
        binding.toGoTo5.setOnClickListener { timeoutToGo5Action() }
        binding.toGoTo30.setOnClickListener { timeoutToGo30Action() }

        updateJamNumberText()
        updateStartOfGameWrapperVisibility()
        updateTimeoutVisibilityFromState()

        if (dataHelper.jamTimerCounting()) {
            startTimer()
        } else {
            stopTimer()
            updateJamTimeFromState()
            updatePeriodTimeFromState()
            updateThirtySecondsFromState()
        }

        jamTimer.schedule(TimeTask(), 0, 20)
    }

    override fun onDestroy() {
        super.onDestroy()
        jamTimer.cancel()
    }

    private inner class TimeTask : TimerTask() {
        override fun run() {
            if (dataHelper.timeoutCounting()) {
                val startTimeout = dataHelper.startTimeoutTime()
                if (startTimeout != null) {
                    val elapsedTimeout = Date().time - startTimeout.time
                    runOnUiThread {
                        if (isCountUpTimeout()) {
                            updateTimeoutCountUpText(elapsedTimeout)
                        } else {
                            val remainingTimeout = TIMEOUT_DURATION_MS - elapsedTimeout
                            updateTimeoutCountdownText(remainingTimeout)
                        }
                    }
                }
                runOnUiThread {
                    binding.timeoutButtonsWrapper.visibility = View.INVISIBLE
                    binding.timeoutWrapper.visibility = View.VISIBLE
                    binding.thirtySeconds.visibility = View.INVISIBLE
                }

                val startJam = dataHelper.startJamTime()
                if (startJam != null) {
                    dataHelper.setStartJamTime(Date(startJam.time + 20))
                }
                val stopJam = dataHelper.stopJamTime()
                if (stopJam != null) {
                    dataHelper.setStopJamTime(Date(stopJam.time + 20))
                }
                val startPeriod = dataHelper.startPeriodTime()
                if (startPeriod != null) {
                    dataHelper.setStartPeriodTime(Date(startPeriod.time + 20))
                }
                return
            } else {
                runOnUiThread {
                    binding.timeoutButtonsWrapper.visibility = View.VISIBLE
                    binding.timeoutWrapper.visibility = View.INVISIBLE
                }
            }

            if (dataHelper.jamTimerCounting()) {
                val startJam = dataHelper.startJamTime()
                if (startJam != null) {
                    val elapsedJam = Date().time - startJam.time
                    val remainingJam = JAM_DURATION_MS - elapsedJam
                    runOnUiThread {
                        updateJamTimeText(remainingJam)
                    }
                }
            } else {
                val stopJam = dataHelper.stopJamTime()
                if (stopJam != null) {
                    val elapsedStop = Date().time - stopJam.time
                    val duration = dataHelper.stopJamDurationMs()
                    val remainingThirty = duration - elapsedStop
                    runOnUiThread {
                        updateThirtySecondsText(remainingThirty)
                    }
                } else {
                    runOnUiThread {
                        binding.thirtySeconds.visibility = View.INVISIBLE
                    }
                }

                if (dataHelper.pausePeriodDuringThirtySeconds()) {
                    val startPeriod = dataHelper.startPeriodTime()
                    if (startPeriod != null) {
                        dataHelper.setStartPeriodTime(Date(startPeriod.time + 20))
                    }
                }
            }

            val startPeriod = dataHelper.startPeriodTime()
            val periodState = dataHelper.periodState()
            if (periodState == DataHelper.STATE_HALFTIME) {
                if (startPeriod != null) {
                    val elapsedPeriod = Date().time - startPeriod.time
                    val remainingHalftime = HALFTIME_DURATION_MS - elapsedPeriod
                    if (remainingHalftime <= 0) {
                        if (dataHelper.jamTimerCounting()) {
                            runOnUiThread {
                                updatePeriodTimeText(0)
                            }
                        } else {
                            val now = Date()
                            dataHelper.setPeriodState(DataHelper.STATE_PERIOD_2)
                            dataHelper.setStartPeriodTime(now)
                            runOnUiThread {
                                updatePeriodTitleText()
                            }
                        }
                    } else {
                        runOnUiThread {
                            updatePeriodTimeText(remainingHalftime)
                        }
                    }
                }
            } else if (startPeriod != null) {
                val elapsedPeriod = Date().time - startPeriod.time
                val remainingPeriod = PERIOD_DURATION_MS - elapsedPeriod
                if (remainingPeriod <= 0) {
                    if (dataHelper.jamTimerCounting()) {
                        runOnUiThread {
                            updatePeriodTimeText(0)
                        }
                    } else {
                        if (periodState == DataHelper.STATE_PERIOD_1) {
                            val now = Date()
                            dataHelper.setPeriodState(DataHelper.STATE_HALFTIME)
                            dataHelper.setStartPeriodTime(now)
                            runOnUiThread {
                                updatePeriodTitleText()
                            }
                        } else {
                            dataHelper.setStartPeriodTime(null)
                            runOnUiThread {
                                updatePeriodTimeText(0)
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        updatePeriodTimeText(remainingPeriod)
                    }
                }
            }
        }
    }

    private fun updateJamNumberText() {
        binding.jamNumber.text = getString(R.string.jam_number, dataHelper.jamNumber())
    }

    private fun updatePeriodTitleText() {
        when (dataHelper.periodState()) {
            DataHelper.STATE_HALFTIME -> {
                binding.periodNumber.text = getString(R.string.halftime)
            }
            DataHelper.STATE_PERIOD_2 -> {
                binding.periodNumber.text = getString(R.string.period_2)
            }
            else -> {
                binding.periodNumber.text = getString(R.string.period_1)
            }
        }
    }

    private fun updateJamTimeFromState() {
        val start = dataHelper.startJamTime()
        if ((dataHelper.jamTimerCounting()) && (start != null)) {
            val elapsed = Date().time - start.time
            val remaining = JAM_DURATION_MS - elapsed
            updateJamTimeText(remaining)
        } else {
            updateJamTimeText(JAM_DURATION_MS)
        }
    }

    private fun updatePeriodTimeFromState() {
        updatePeriodTitleText()
        val start = dataHelper.startPeriodTime()
        val periodState = dataHelper.periodState()
        if (periodState == DataHelper.STATE_HALFTIME) {
            if (start != null) {
                val elapsed = Date().time - start.time
                val remaining = HALFTIME_DURATION_MS - elapsed
                if (remaining <= 0 && dataHelper.jamTimerCounting()) {
                    updatePeriodTimeText(0)
                } else {
                    updatePeriodTimeText(remaining)
                }
            } else {
                updatePeriodTimeText(HALFTIME_DURATION_MS)
            }
        } else if (start != null) {
            val elapsed = Date().time - start.time
            val remaining = PERIOD_DURATION_MS - elapsed
            if (remaining <= 0 && dataHelper.jamTimerCounting()) {
                updatePeriodTimeText(0)
            } else {
                updatePeriodTimeText(remaining)
            }
        } else {
            updatePeriodTimeText(PERIOD_DURATION_MS)
        }
    }

    private fun updateThirtySecondsFromState() {
        val stopJam = dataHelper.stopJamTime()
        if (!dataHelper.jamTimerCounting() && stopJam != null) {
            val elapsed = Date().time - stopJam.time
            val duration = dataHelper.stopJamDurationMs()
            val remainingThirty = duration - elapsed
            updateThirtySecondsText(remainingThirty)
        } else {
            binding.thirtySeconds.visibility = View.INVISIBLE
        }
    }

    private fun updateJamTimeText(remainingMs: Long) {
        binding.jamTime.text = TimeUtils.smallDecimalSpannable(TimeUtils.jamTimeStringFromLong(remainingMs, allowNegative = true))
        if (remainingMs <= WARNING_THRESHOLD_MS) {
            binding.jamTime.setTextColor(Color.RED)
        } else {
            binding.jamTime.setTextColor(Color.BLACK)
        }
    }

    private fun updatePeriodTimeText(remainingMs: Long) {
        val safeMs = if (remainingMs < 0) 0L else remainingMs
        binding.periodTime.text = TimeUtils.timeStringFromLong(safeMs)
    }

    private fun updateThirtySecondsText(remainingMs: Long) {
        binding.thirtySeconds.visibility = View.VISIBLE
        binding.thirtySeconds.text = TimeUtils.smallDecimalSpannable(TimeUtils.thirtySecondsStringFromLong(remainingMs))
        if (remainingMs <= THIRTY_WARNING_THRESHOLD_MS) {
            binding.thirtySeconds.setTextColor(Color.RED)
        } else {
            binding.thirtySeconds.setTextColor(Color.BLACK)
        }
    }

    private fun resetAction() {
        dataHelper.setStopJamTime(null)
        dataHelper.setStartJamTime(null)
        dataHelper.setStartPeriodTime(null)
        dataHelper.setTimeoutCounting(false)
        dataHelper.setStartTimeoutTime(null)
        dataHelper.setTimeoutType(null)
        dataHelper.setStopJamDurationMs(THIRTY_SECONDS_MS)
        dataHelper.setPausePeriodDuringThirtySeconds(false)
        dataHelper.setJamNumber(1)
        dataHelper.setPeriodState(DataHelper.STATE_PERIOD_1)
        dataHelper.setHasStartedAnyJam(false)
        stopTimer()
        updateStartOfGameWrapperVisibility()
        binding.thirtySeconds.visibility = View.INVISIBLE
        binding.timeoutButtonsWrapper.visibility = View.VISIBLE
        binding.timeoutWrapper.visibility = View.INVISIBLE
        updateJamTimeText(JAM_DURATION_MS)
        updatePeriodTitleText()
        updatePeriodTimeText(PERIOD_DURATION_MS)
        updateJamNumberText()
    }

    private fun goTo30Action() {
        dataHelper.setStopJamDurationMs(THIRTY_SECONDS_MS)
        dataHelper.setStopJamTime(Date())
        dataHelper.setPausePeriodDuringThirtySeconds(true)
        dataHelper.setHasStartedAnyJam(true)
        updateStartOfGameWrapperVisibility()
    }

    private fun goTo5Action() {
        val durationMs = 5 * 1000L
        dataHelper.setStopJamDurationMs(durationMs)
        dataHelper.setStopJamTime(Date())
        dataHelper.setPausePeriodDuringThirtySeconds(true)
        dataHelper.setHasStartedAnyJam(true)
        updateStartOfGameWrapperVisibility()
    }

    private fun stopTimer() {
        dataHelper.setJamTimerCounting(value = false)
        binding.jamStartButton.text = getString(R.string.start_jam)
        dataHelper.setStopJamDurationMs(THIRTY_SECONDS_MS)
        dataHelper.setPausePeriodDuringThirtySeconds(false)

        val startPeriod = dataHelper.startPeriodTime()
        val periodState = dataHelper.periodState()
        var periodExpired = false
        if (startPeriod != null) {
            val elapsedPeriod = Date().time - startPeriod.time
            if (periodState == DataHelper.STATE_HALFTIME && elapsedPeriod >= HALFTIME_DURATION_MS) {
                periodExpired = true
            } else if (periodState != DataHelper.STATE_HALFTIME && elapsedPeriod >= PERIOD_DURATION_MS) {
                periodExpired = true
            }
        }

        if (periodExpired) {
            dataHelper.setStopJamTime(null)
        }

        val stopJam = dataHelper.stopJamTime()
        if (stopJam != null) {
            val elapsed = Date().time - stopJam.time
            val remainingThirty = THIRTY_SECONDS_MS - elapsed
            updateThirtySecondsText(remainingThirty)
        } else {
            binding.thirtySeconds.visibility = View.INVISIBLE
        }
    }

    private fun startTimer() {
        dataHelper.setJamTimerCounting(value = true)
        binding.jamStartButton.text = getString(R.string.stop_jam)
        binding.thirtySeconds.visibility = View.INVISIBLE
    }

    private fun startStopAction() {
        if (dataHelper.timeoutCounting()) {
            dataHelper.setTimeoutCounting(false)
            dataHelper.setStartTimeoutTime(null)
            dataHelper.setTimeoutType(null)
            binding.timeoutWrapper.visibility = View.INVISIBLE
            binding.timeoutButtonsWrapper.visibility = View.VISIBLE
        }

        dataHelper.setPausePeriodDuringThirtySeconds(false)

        if (dataHelper.jamTimerCounting()) {
            dataHelper.setStopJamTime(Date())
            stopTimer()
            checkPeriodExpirationAfterJam()
        } else {
            dataHelper.setHasStartedAnyJam(true)
            updateStartOfGameWrapperVisibility()
            // Increment jam number if starting a subsequent jam
            if (dataHelper.stopJamTime() != null) {
                dataHelper.setJamNumber(dataHelper.jamNumber() + 1)
            }
            updateJamNumberText()

            // Always start a fresh 2-minute Jam
            dataHelper.setStartJamTime(Date())
            dataHelper.setStopJamTime(null)

            // Period state handling:
            if (dataHelper.periodState() == DataHelper.STATE_HALFTIME) {
                // Starting a jam during Halftime transitions to Period 2
                dataHelper.setPeriodState(DataHelper.STATE_PERIOD_2)
                dataHelper.setStartPeriodTime(Date())
                updatePeriodTitleText()
            } else if (dataHelper.startPeriodTime() == null) {
                dataHelper.setStartPeriodTime(Date())
            }

            startTimer()
        }
    }

    private fun checkPeriodExpirationAfterJam() {
        val startPeriod = dataHelper.startPeriodTime()
        val periodState = dataHelper.periodState()
        if (startPeriod != null) {
            val elapsedPeriod = Date().time - startPeriod.time
            val now = Date()
            if (periodState == DataHelper.STATE_HALFTIME) {
                if (elapsedPeriod >= HALFTIME_DURATION_MS) {
                    dataHelper.setPeriodState(DataHelper.STATE_PERIOD_2)
                    dataHelper.setStartPeriodTime(now)
                    dataHelper.setStopJamTime(null)
                    runOnUiThread {
                        updatePeriodTitleText()
                        updatePeriodTimeFromState()
                        binding.thirtySeconds.visibility = View.INVISIBLE
                    }
                }
            } else if (periodState == DataHelper.STATE_PERIOD_1) {
                if (elapsedPeriod >= PERIOD_DURATION_MS) {
                    dataHelper.setPeriodState(DataHelper.STATE_HALFTIME)
                    dataHelper.setStartPeriodTime(now)
                    dataHelper.setStopJamTime(null)
                    runOnUiThread {
                        updatePeriodTitleText()
                        updatePeriodTimeFromState()
                        binding.thirtySeconds.visibility = View.INVISIBLE
                    }
                }
            } else if (periodState == DataHelper.STATE_PERIOD_2) {
                if (elapsedPeriod >= PERIOD_DURATION_MS) {
                    dataHelper.setStartPeriodTime(null)
                    dataHelper.setStopJamTime(null)
                    runOnUiThread {
                        updatePeriodTimeText(0)
                        binding.thirtySeconds.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun updateStartOfGameWrapperVisibility() {
        if (dataHelper.hasStartedAnyJam()) {
            binding.startOfGameWrapper.visibility = View.INVISIBLE
        } else {
            binding.startOfGameWrapper.visibility = View.VISIBLE
        }
    }

    private fun isCountUpTimeout(): Boolean {
        val type = dataHelper.timeoutType()
        return type == getString(R.string.official_timeout) || type == getString(R.string.emergency_stop)
    }

    private fun startTimeoutAction(typeName: String) {
        if (dataHelper.jamTimerCounting()) {
            dataHelper.setStopJamTime(Date())
            stopTimer()
            checkPeriodExpirationAfterJam()
        }

        dataHelper.setTimeoutCounting(true)
        dataHelper.setTimeoutType(typeName)
        dataHelper.setStartTimeoutTime(Date())
        binding.timeOutType.text = typeName
        binding.timeoutButtonsWrapper.visibility = View.INVISIBLE
        binding.timeoutWrapper.visibility = View.VISIBLE
        binding.thirtySeconds.visibility = View.INVISIBLE
    }

    private fun timeoutToGo5Action() {
        dataHelper.setTimeoutCounting(false)
        dataHelper.setStartTimeoutTime(null)
        dataHelper.setTimeoutType(null)
        binding.timeoutWrapper.visibility = View.INVISIBLE
        binding.timeoutButtonsWrapper.visibility = View.VISIBLE
        goTo5Action()
    }

    private fun timeoutToGo30Action() {
        dataHelper.setTimeoutCounting(false)
        dataHelper.setStartTimeoutTime(null)
        dataHelper.setTimeoutType(null)
        binding.timeoutWrapper.visibility = View.INVISIBLE
        binding.timeoutButtonsWrapper.visibility = View.VISIBLE
        goTo30Action()
    }

    private fun updateTimeoutVisibilityFromState() {
        if (dataHelper.timeoutCounting()) {
            binding.timeoutButtonsWrapper.visibility = View.INVISIBLE
            binding.timeoutWrapper.visibility = View.VISIBLE
            binding.thirtySeconds.visibility = View.INVISIBLE
            val type = dataHelper.timeoutType()
            binding.timeOutType.text = type ?: getString(R.string.official_timeout)
            val start = dataHelper.startTimeoutTime()
            if (start != null) {
                val elapsed = Date().time - start.time
                if (isCountUpTimeout()) {
                    updateTimeoutCountUpText(elapsed)
                } else {
                    val remaining = TIMEOUT_DURATION_MS - elapsed
                    updateTimeoutCountdownText(remaining)
                }
            } else {
                if (isCountUpTimeout()) {
                    updateTimeoutCountUpText(0L)
                } else {
                    updateTimeoutCountdownText(TIMEOUT_DURATION_MS)
                }
            }
        } else {
            binding.timeoutButtonsWrapper.visibility = View.VISIBLE
            binding.timeoutWrapper.visibility = View.INVISIBLE
        }
    }

    private fun updateTimeoutCountUpText(elapsedMs: Long) {
        val safeMs = if (elapsedMs < 0) 0L else elapsedMs
        binding.timeoutTime.text = TimeUtils.smallDecimalSpannable(TimeUtils.elapsedTimeStringFromLong(safeMs))
        binding.timeoutTime.setTextColor(Color.BLACK)
    }

    private fun updateTimeoutCountdownText(remainingMs: Long) {
        binding.timeoutTime.text = TimeUtils.smallDecimalSpannable(TimeUtils.jamTimeStringFromLong(remainingMs, allowNegative = true))
        if (remainingMs <= 0) {
            binding.timeoutTime.setTextColor(Color.RED)
        } else {
            binding.timeoutTime.setTextColor(Color.BLACK)
        }
    }

    companion object {
        private const val JAM_DURATION_MS = 2 * 60 * 1000L
        private const val PERIOD_DURATION_MS = 30 * 60 * 1000L
        private const val HALFTIME_DURATION_MS = 15 * 60 * 1000L
        private const val THIRTY_SECONDS_MS = 30 * 1000L
        private const val TIMEOUT_DURATION_MS = 1 * 60 * 1000L
        private const val WARNING_THRESHOLD_MS = 6000L
        private const val THIRTY_WARNING_THRESHOLD_MS = 5000L
    }
}
