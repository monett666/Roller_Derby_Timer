package cz.motikator.rollerderbytimer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import cz.motikator.rollerderbytimer.databinding.ActivityPenaltyBoxTimerBinding
import cz.motikator.rollerderbytimer.databinding.ItemPenaltyTimerBinding
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class PenaltyBoxTimerActivity : ComponentActivity() {

    lateinit var binding: ActivityPenaltyBoxTimerBinding
    private val timer = Timer()
    private val penaltyTimers = mutableListOf<PenaltyTimerController>()
    private var isStopAllActive = false

    class PenaltyTimerController(
        val label: String,
        val itemBinding: ItemPenaltyTimerBinding
    ) {
        var remainingMs: Long = 30 * 1000L
        var isCounting: Boolean = false
        var wasCountingBeforeStopAll: Boolean = false

        init {
            itemBinding.timerLabel.text = label
            updateDisplay()
        }

        fun updateDisplay() {
            val safeMs = if (remainingMs < 0) 0L else remainingMs
            val totalSeconds = (safeMs + 999) / 1000
            itemBinding.timerText.text = "${totalSeconds}s"
            if (remainingMs <= 0) {
                itemBinding.timerText.setTextColor(Color.RED)
            } else {
                itemBinding.timerText.setTextColor(Color.BLACK)
            }
        }
    }

    private fun getTotalPausedCount(): Int {
        return penaltyTimers.count { it.wasCountingBeforeStopAll }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenaltyBoxTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val team1Timers = listOf(
            PenaltyTimerController("Jammer", binding.team1Jammer),
            PenaltyTimerController("Blocker 1", binding.team1Blocker1),
            PenaltyTimerController("Blocker 2", binding.team1Blocker2),
            PenaltyTimerController("Blocker 3", binding.team1Blocker3)
        )

        val team2Timers = listOf(
            PenaltyTimerController("Jammer", binding.team2Jammer),
            PenaltyTimerController("Blocker 1", binding.team2Blocker1),
            PenaltyTimerController("Blocker 2", binding.team2Blocker2),
            PenaltyTimerController("Blocker 3", binding.team2Blocker3)
        )

        penaltyTimers.addAll(team1Timers)
        penaltyTimers.addAll(team2Timers)

        for (pt in penaltyTimers) {
            pt.itemBinding.startTimerButton.setOnClickListener {
                if (pt.isCounting) {
                    pt.isCounting = false
                    pt.wasCountingBeforeStopAll = false
                    pt.itemBinding.startTimerButton.text = getString(R.string.start)
                } else {
                    if (isStopAllActive && getTotalPausedCount() > 1) {
                        // Locked by Stop All when multiple timers are active
                        return@setOnClickListener
                    }
                    if (pt.remainingMs <= 0) {
                        pt.remainingMs = 30 * 1000L
                    }
                    pt.isCounting = true
                    pt.wasCountingBeforeStopAll = false
                    pt.itemBinding.startTimerButton.text = getString(R.string.stop)
                    isStopAllActive = false
                    binding.stopAllButton.text = getString(R.string.stop_all)
                }
            }
            pt.itemBinding.restartTimerButton.setOnClickListener {
                pt.isCounting = false
                pt.wasCountingBeforeStopAll = false
                pt.remainingMs = 30 * 1000L
                pt.updateDisplay()
                pt.itemBinding.startTimerButton.text = getString(R.string.start)
            }
            pt.itemBinding.plus30Button.setOnClickListener {
                pt.remainingMs += 30 * 1000L
                pt.updateDisplay()
            }
        }

        binding.stopAllButton.setOnClickListener {
            if (!isStopAllActive) {
                // Stop All
                val countingList = penaltyTimers.filter { it.isCounting }
                if (countingList.isNotEmpty()) {
                    isStopAllActive = true
                    for (pt in penaltyTimers) {
                        if (pt.isCounting) {
                            pt.wasCountingBeforeStopAll = true
                            pt.isCounting = false
                            pt.itemBinding.startTimerButton.text = getString(R.string.start)
                        } else {
                            pt.wasCountingBeforeStopAll = false
                        }
                    }
                    binding.stopAllButton.text = getString(R.string.resume_all)
                }
            } else {
                // Resume All
                isStopAllActive = false
                for (pt in penaltyTimers) {
                    if (pt.wasCountingBeforeStopAll) {
                        pt.isCounting = true
                        pt.wasCountingBeforeStopAll = false
                        pt.itemBinding.startTimerButton.text = getString(R.string.stop)
                    }
                }
                binding.stopAllButton.text = getString(R.string.stop_all)
            }
        }

        binding.restartAllButton.setOnClickListener {
            isStopAllActive = false
            binding.stopAllButton.text = getString(R.string.stop_all)
            for (pt in penaltyTimers) {
                pt.isCounting = false
                pt.wasCountingBeforeStopAll = false
                pt.remainingMs = 30 * 1000L
                pt.updateDisplay()
                pt.itemBinding.startTimerButton.text = getString(R.string.start)
            }
        }

        timer.schedule(object : TimerTask() {
            var lastTick = System.currentTimeMillis()
            override fun run() {
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now

                var anyChanged = false
                for (pt in penaltyTimers) {
                    if (pt.isCounting) {
                        pt.remainingMs -= delta
                        if (pt.remainingMs < 0) {
                            pt.remainingMs = 0
                            pt.isCounting = false
                            pt.wasCountingBeforeStopAll = false
                        }
                        anyChanged = true
                    }
                }

                if (anyChanged) {
                    runOnUiThread {
                        for (pt in penaltyTimers) {
                            pt.updateDisplay()
                            if (!pt.isCounting && pt.remainingMs == 0L) {
                                pt.itemBinding.startTimerButton.text = getString(R.string.start)
                            }
                        }
                    }
                }
            }
        }, 0, 50)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}
