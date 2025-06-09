package com.example.pocketmoneypoe

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore

class ChartActivity : AppCompatActivity() {

    private lateinit var combinedChart: CombinedChart
    private lateinit var chartContainer: LinearLayout

    private var minGoal: Float? = null
    private var maxGoal: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        combinedChart = findViewById(R.id.combinedChart)
        chartContainer = findViewById(R.id.chartContainer)

        // Retrieve floats from intent extras with default fallback -1f means missing
        minGoal = intent.getFloatExtra("MIN_GOAL", -1f)
        maxGoal = intent.getFloatExtra("MAX_GOAL", -1f)

        if (minGoal == -1f || maxGoal == -1f) {
            Toast.makeText(this, "Min or Max goal not found!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchDataAndSetupChart()
    }

    private fun fetchDataAndSetupChart() {
        val db = FirebaseFirestore.getInstance()

        db.collection("expenses")
            .get()
            .addOnSuccessListener { documents ->
                val categorySums = mutableMapOf<String, Float>()

                for (doc in documents) {
                    val category = doc.getString("category") ?: "Unknown"
                    val amount = doc.getDouble("amount")?.toFloat() ?: 0f
                    categorySums[category] = (categorySums[category] ?: 0f) + amount
                }

                val categories = categorySums.keys.sorted()
                val spendingValues = categories.map { categorySums[it] ?: 0f }

                if (categories.isEmpty()) {
                    Toast.makeText(this, "No expenses data found", Toast.LENGTH_SHORT).show()
                    combinedChart.clear()
                    combinedChart.setNoDataText("No chart data available")
                } else {
                    setupChart(categories, spendingValues)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                combinedChart.clear()
                combinedChart.setNoDataText("No chart data available")
            }
    }

    private fun setupChart(categories: List<String>, spendingValues: List<Float>) {
        if (categories.isEmpty() || spendingValues.isEmpty()) {
            combinedChart.clear()
            combinedChart.setNoDataText("No chart data available")
            return
        }

        val barWidthDp = 60
        val spacingDp = 15
        val totalWidthDp = categories.size * (barWidthDp + spacingDp)
        val totalWidthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            totalWidthDp.toFloat(),
            resources.displayMetrics
        ).toInt()

        val params = combinedChart.layoutParams
        params.width = totalWidthPx
        combinedChart.layoutParams = params

        val barEntries = spendingValues.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value)
        }
        val barDataSet = BarDataSet(barEntries, "Expenses").apply {
            color = Color.BLUE
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        val barData = BarData(barDataSet).apply {
            barWidth = 0.5f
        }

        val mid = (minGoal!! + maxGoal!!) / 2
        val lineEntries = spendingValues.indices.map { index ->
            Entry(index.toFloat(), mid)
        }
        val lineDataSet = LineDataSet(lineEntries, "Average Goal").apply {
            color = Color.MAGENTA
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
        }
        val lineData = LineData(lineDataSet)

        val combinedData = CombinedData()
        combinedData.setData(barData)
        combinedData.setData(lineData)
        combinedChart.data = combinedData

        val xAxis = combinedChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(categories)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = categories.size
        xAxis.labelRotationAngle = -30f

        val leftAxis: YAxis = combinedChart.axisLeft
        combinedChart.axisRight.isEnabled = false

        val minLine = LimitLine(minGoal!!, "Min Goal").apply {
            lineColor = Color.GREEN
            lineWidth = 2f
            textColor = Color.GREEN
            textSize = 12f
        }
        val maxLine = LimitLine(maxGoal!!, "Max Goal").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
        }

        leftAxis.removeAllLimitLines()
        leftAxis.addLimitLine(minLine)
        leftAxis.addLimitLine(maxLine)

        val maxSpending = spendingValues.maxOrNull() ?: 0f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = maxOf(maxGoal!!, maxSpending) * 1.2f

        combinedChart.setExtraOffsets(0f, 0f, 0f, 20f)
        combinedChart.description.isEnabled = false
        combinedChart.animateY(1000)
        combinedChart.invalidate()
    }
}
