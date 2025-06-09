package com.example.pocketmoneypoe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pocketmoneypoe.databinding.ActivityGoalsBinding

class GoalsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackSettings.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.goalsConstraintLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMax.setOnClickListener {
            val max = binding.editMax.text.toString()
            if (max.isNotBlank()) {
                binding.editMax.setText("R$max")
                Toast.makeText(this, "Max Goal: R$max", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a max goal", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnMin.setOnClickListener {
            val min = binding.editMin.text.toString()
            if (min.isNotBlank()) {
                binding.editMin.setText("R$min")
                Toast.makeText(this, "Minimum Goal: R$min", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a minimum goal", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnViewChart.setOnClickListener {
            // Remove "R" and trim spaces
            val minText = binding.editMin.text.toString().replace("R", "").trim()
            val maxText = binding.editMax.text.toString().replace("R", "").trim()

            if (minText.isNotBlank() && maxText.isNotBlank()) {
                val min = minText.toFloatOrNull()
                val max = maxText.toFloatOrNull()

                if (min != null && max != null) {
                    val intent = Intent(this, ChartActivity::class.java)
                    intent.putExtra("MIN_GOAL", min)   // Pass floats, not strings
                    intent.putExtra("MAX_GOAL", max)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in both min and max goals", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVisualOverview.setOnClickListener {
            val minText = binding.editMin.text.toString().replace("R", "").trim()
            val maxText = binding.editMax.text.toString().replace("R", "").trim()

            if (minText.isNotBlank() && maxText.isNotBlank()) {
                val min = minText.toFloatOrNull()
                val max = maxText.toFloatOrNull()

                if (min != null && max != null) {
                    val intent = Intent(this, SpendingOverviewActivity::class.java)
                    intent.putExtra("MIN_GOAL", min)
                    intent.putExtra("MAX_GOAL", max)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter both goals first", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
