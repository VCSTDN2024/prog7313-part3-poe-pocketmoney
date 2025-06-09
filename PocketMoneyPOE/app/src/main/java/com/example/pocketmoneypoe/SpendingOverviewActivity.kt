package com.example.pocketmoneypoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmoneypoe.databinding.ActivitySpendingOverviewBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class SpendingOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpendingOverviewBinding
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpendingOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setBackgroundResource(R.drawable.background)

        val minGoal = intent.getFloatExtra("MIN_GOAL", 0f)
        val maxGoal = intent.getFloatExtra("MAX_GOAL", 0f)

        fetchExpensesAndDisplayChart(minGoal, maxGoal)
    }

    private fun fetchExpensesAndDisplayChart(min: Float, max: Float) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val oneMonthAgo = calendar.time

        db.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    binding.totalTextView.text = "Error loading data"
                    binding.statusTextView.text = ""
                    return@addSnapshotListener
                }

                var totalAmount = 0f

                for (doc in snapshot.documents) {
                    val timestamp = doc.getTimestamp("date")?.toDate()
                    val amount = doc.getDouble("amount")?.toFloat() ?: 0f

                    if (timestamp != null && timestamp.after(oneMonthAgo)) {
                        totalAmount += amount
                    }
                }

                // Display total for last 30 days
                binding.totalTextView.text = "Total spent in last 30 days: R%.2f".format(totalAmount)

                // Determine status message
                val statusMessage = when {
                    totalAmount < min -> "You are below your minimum goal."
                    totalAmount > max -> "You have exceeded your maximum goal."
                    else -> "You are within your spending goals."
                }
                binding.statusTextView.text = statusMessage

                // Bar entry for total
                val entries = listOf(BarEntry(0f, totalAmount))

                val barDataSet = BarDataSet(entries, "Total Spending").apply {
                    color = getColor(R.color.teal_700)
                    valueTextSize = 14f
                    valueTextColor = getColor(R.color.white)
                    setDrawValues(true)
                }

                val barData = BarData(barDataSet)
                barData.barWidth = 0.5f

                val chart = binding.lineChart as com.github.mikephil.charting.charts.BarChart
                chart.data = barData

                // Y-axis setup
                val yAxis = chart.axisLeft
                yAxis.removeAllLimitLines()

                val minLine = LimitLine(min, "Min Goal").apply {
                    lineColor = getColor(R.color.green)
                    lineWidth = 2f
                    textColor = getColor(R.color.green)
                    textSize = 12f
                }

                val maxLine = LimitLine(max, "Max Goal").apply {
                    lineColor = getColor(R.color.red)
                    lineWidth = 2f
                    textColor = getColor(R.color.red)
                    textSize = 12f
                }

                yAxis.addLimitLine(minLine)
                yAxis.addLimitLine(maxLine)

                yAxis.axisMinimum = 0f
                yAxis.axisMaximum = (max.coerceAtLeast(totalAmount) * 1.1f)
                yAxis.textColor = getColor(R.color.white)
                yAxis.gridColor = getColor(R.color.white)

                // X-axis setup
                val xAxis = chart.xAxis
                xAxis.granularity = 1f
                xAxis.setDrawLabels(false)
                xAxis.setDrawGridLines(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = getColor(R.color.white)

                chart.axisRight.isEnabled = false
                chart.description.text = "Total spending last 30 days with Goals"
                chart.description.textColor = getColor(R.color.white)
                chart.legend.isEnabled = false
                chart.setFitBars(true)
                chart.invalidate()
            }
    }
}
