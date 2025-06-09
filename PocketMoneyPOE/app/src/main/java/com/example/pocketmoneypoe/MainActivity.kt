package com.example.pocketmoneypoe

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmoneypoe.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show daily tip
        val popup = binding.dailyTipPopup
        val message = binding.dailyMessage
        val closeBtn = binding.closePopup

        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val tip = when (dayOfWeek) {
            Calendar.MONDAY -> "It's Monday! Save on 4 purchases today 💰"
            Calendar.TUESDAY -> "Tip Tuesday: Track your food spending today 🍕"
            Calendar.WEDNESDAY -> "Midweek check! Review your budget goals 📊"
            Calendar.THURSDAY -> "Almost there! Small savings add up 🪙"
            Calendar.FRIDAY -> "Friday Treat? Set a limit before spending 🛍️"
            Calendar.SATURDAY -> "Weekend fun! Budget entertainment 🎬"
            Calendar.SUNDAY -> "Plan your week ahead. Budget your income 📝"
            else -> "Welcome back! Stay smart with your spending."
        }
        message.text = tip

        closeBtn.setOnClickListener {
            popup.visibility = View.GONE
        }

        // Navigate to UserProfileActivity
        binding.userprofile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        // Navigate to SettingsActivity
        binding.settingsbar.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Navigate to the add expenses page
        binding.expense.setOnClickListener {
            val intent = Intent(this, ExpensesActivity::class.java)
            startActivity(intent)
        }

        // navigate to total category page
        binding.categorytotals.setOnClickListener {
            val intent = Intent(this, CategoryTotals::class.java)
            startActivity(intent)
        }

        // navigate to total expenses page
        binding.expensestotals.setOnClickListener {
            val intent = Intent(this, ExpensesTotals::class.java)
            startActivity(intent)
        }

        // navigate to the goals page
        binding.goals.setOnClickListener{
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }
    }
}
