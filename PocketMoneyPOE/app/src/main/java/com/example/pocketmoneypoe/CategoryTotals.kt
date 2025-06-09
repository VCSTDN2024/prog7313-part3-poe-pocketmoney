package com.example.pocketmoneypoe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmoneypoe.databinding.ActivityCategoryTotalsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CategoryTotals : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryTotalsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var tableLayout: TableLayout
    private lateinit var btnRange: Button
    private lateinit var rangeView: TextView

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // binding
        binding = ActivityCategoryTotalsBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_category_totals)

        binding.btnBackSettings.setOnClickListener {
            val intent = (Intent(this, MainActivity::class.java))
            startActivity(intent)
        }

        db = FirebaseFirestore.getInstance()

        tableLayout = findViewById(R.id.categoryTotalsTableLayout)
        btnRange = findViewById(R.id.btnRange)
        rangeView = findViewById(R.id.rangeView)
        // Date picker
        btnRange.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build()

            picker.show(supportFragmentManager, "range_picker")
            picker.addOnPositiveButtonClickListener { selection ->
                startDate = Date(selection.first ?: 0)
                endDate = Date(selection.second ?: 0)

                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedStart = format.format(startDate!!)
                val formattedEnd = format.format(endDate!!)

                rangeView.text = "Start: $formattedStart\nEnd: $formattedEnd"
                loadExpenses()
            }
        }

        // Load all data on start
        loadExpenses()
    }

    // loads expense data
    private fun loadExpenses() {



        db.collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                val categoryTotals = HashMap<String, Double>()

                for (document in result) {
                    val category = document.getString("category") ?: "Other"
                    val amount = document.getDouble("amount") ?: 0.0
                    val timestamp = document.getTimestamp("date")?.toDate()

                    if (startDate != null && endDate != null) {
                        if (timestamp == null || timestamp.before(startDate) || timestamp.after(endDate)) {
                            continue
                        }
                    }

                    categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + amount
                }

                // Sort categories alphabetically
                val sortedCategories = categoryTotals.toSortedMap()

                // Clear previous rows (preserve header)
                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }

                for ((category, total) in sortedCategories) {
                    val row = TableRow(this)
                    val categoryText = TextView(this).apply {
                        text = category
                        setPadding(16, 16, 16, 16)
                    }

                    val totalText = TextView(this).apply {
                        text = "R${String.format("%.2f", total)}"
                        setPadding(16, 16, 16, 16)
                    }

                    row.addView(categoryText)
                    row.addView(totalText)
                    tableLayout.addView(row)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
    }
}
