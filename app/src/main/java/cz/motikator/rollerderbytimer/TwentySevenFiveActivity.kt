package cz.motikator.rollerderbytimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import cz.motikator.rollerderbytimer.databinding.ActivityTwentySevenFiveBinding
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class TwentySevenFiveActivity : ComponentActivity() {

    lateinit var binding: ActivityTwentySevenFiveBinding
    private val timer = Timer()
    private var remainingTimeMs = FIVE_MINUTES_MS
    private var isCounting = false
    private var counterValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwentySevenFiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTimerText()
        updateCounterText()

        binding.startButton.setOnClickListener {
            if (isCounting) {
                // Restart / reset action
                isCounting = false
                remainingTimeMs = FIVE_MINUTES_MS
                counterValue = 0
                updateTimerText()
                updateCounterText()
                binding.startButton.text = getString(R.string.start)
            } else {
                // Start action
                isCounting = true
                binding.startButton.text = getString(R.string.restart)
            }
        }

        binding.plusButton.setOnClickListener {
            counterValue++
            updateCounterText()
        }

        binding.minusButton.setOnClickListener {
            if (counterValue > 0) {
                counterValue--
                updateCounterText()
            }
        }

        timer.schedule(object : TimerTask() {
            var lastTick = System.currentTimeMillis()
            override fun run() {
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now

                if (isCounting) {
                    remainingTimeMs -= delta
                    if (remainingTimeMs < 0) {
                        remainingTimeMs = 0
                        isCounting = false
                    }
                    runOnUiThread {
                        updateTimerText()
                        if (!isCounting) {
                            binding.startButton.text = getString(R.string.start)
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

    private fun updateTimerText() {
        val safeMs = if (remainingTimeMs < 0) 0L else remainingTimeMs
        binding.circularProgressBar.max = FIVE_MINUTES_MS.toInt()
        binding.circularProgressBar.progress = safeMs.toInt()
        val totalSeconds = (safeMs + 999) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        binding.timerTextView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun updateCounterText() {
        binding.counterTextView.text = counterValue.toString()
    }

    companion object {
        private const val FIVE_MINUTES_MS = 5 * 60 * 1000L
    }
}
