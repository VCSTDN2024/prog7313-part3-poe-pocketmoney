package com.example.pocketmoneypoe
//importing packages
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmoneypoe.databinding.ActivityExpensesTotalsBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ExpensesTotals : AppCompatActivity() {
    private lateinit var binding: ActivityExpensesTotalsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var tableLayout: TableLayout
    private lateinit var btnRange: Button
    private lateinit var rangeView: TextView
    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // binds
        binding = ActivityExpensesTotalsBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_expenses_totals)

        binding.btnBackSettings.setOnClickListener {
            val intent = (Intent(this, MainActivity::class.java))
            startActivity(intent)
        }

        // create instance of database to access data
        db = FirebaseFirestore.getInstance()

        // instances of xml components to read data from
        tableLayout = findViewById(R.id.expensesTotalsTableLayout)
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

        //Load all data on start
        loadExpenses()
    }

    // loads expense data
    private fun loadExpenses() {

        // accesses the database expense collection
        db.collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                if (tableLayout.childCount > 1) {
                    tableLayout.removeViews(1, tableLayout.childCount - 1)
                }

                for (doc in result) {
                    val timestamp = doc.getTimestamp("date")?.toDate() ?: continue

                    // Check if a date range is selected, and filter accordingly
                    if (startDate != null && endDate != null) {
                        if (timestamp.before(startDate) || timestamp.after(endDate)) {
                            continue  // skip this entry
                        }
                    }

                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = formatter.format(timestamp)

                    val amount = try {
                        val amountValue = doc.get("amount")
                        when (amountValue) {
                            is Number -> amountValue.toDouble().toString()
                            is String -> amountValue.toDoubleOrNull()?.toString() ?: "Invalid"
                            else -> "Invalid"
                        }
                    } catch (e: Exception) {
                        "Invalid"
                    }

                    val transactionType = doc.getString("transactionType") ?: ""
                    val start = doc.getString("start") ?: ""
                    val end = doc.getString("end") ?: ""
                    val category = doc.getString("category") ?: ""

                    val row = TableRow(this)
                    listOf(date, transactionType, amount, start, end, category).forEach {
                        val tv = TextView(this).apply {
                            text = it
                            setPadding(8, 8, 8, 8)
                        }
                        row.addView(tv)
                    }
                    tableLayout.addView(row)
                }

            }

            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
    }
}
