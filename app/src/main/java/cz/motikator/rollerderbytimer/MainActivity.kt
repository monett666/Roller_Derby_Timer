package cz.motikator.rollerderbytimer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import cz.motikator.rollerderbytimer.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.jamTimerButton.setOnClickListener {
            startActivity(Intent(this, JamTimerActivity::class.java))
        }

        binding.twentySevenFiveButton.setOnClickListener {
            startActivity(Intent(this, TwentySevenFiveActivity::class.java))
        }

        binding.penaltyBoxButton.setOnClickListener {
            startActivity(Intent(this, PenaltyBoxTimerActivity::class.java))
        }
    }
}
