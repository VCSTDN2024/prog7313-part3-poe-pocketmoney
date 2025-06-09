package com.example.pocketmoneypoe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmoneypoe.databinding.ActivityExpensesBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null
    private var selectedImageButton: ImageButton? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register image picker
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                selectedImageUri = it.data?.data
                selectedImageButton?.setImageURI(selectedImageUri)
            }
        }

        binding.buttonAddCategory.setOnClickListener {
            val intent = (Intent(this, CategoryActivity::class.java))
            startActivity(intent)
        }

        binding.textViewBack.setOnClickListener {
            val intent = (Intent(this, MainActivity::class.java))
            startActivity(intent)
        }

        binding.buttonAddExpense.setOnClickListener {
            showAddExpenseDialog() // This alone shows the dialog
        }

        loadExpensesFromFirestore()
    }

    private fun showAddExpenseDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val editTextDate = view.findViewById<EditText>(R.id.editTextDate)
        val editTextTransactionType = view.findViewById<EditText>(R.id.editTextTransactionType)
        val editTextAmount = view.findViewById<EditText>(R.id.editTextAmount)
        val editTextStart = view.findViewById<EditText>(R.id.editTextStart)
        val editTextEnd = view.findViewById<EditText>(R.id.editTextEnd)
        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        //val imageButton = view.findViewById<ImageButton>(R.id.buttonUploadImage)

        //selectedImageButton = imageButton

        //imageButton.setOnClickListener {
        //    val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        //    pickImageLauncher.launch(intent)
        //}

        // val categoryList = mutableListOf<String>()
        db.collection("categories").get()
            .addOnSuccessListener { result ->
                val categoryList = mutableListOf<String>()
                for (doc in result) {
                    val name = doc.getString("name")
                    if (!name.isNullOrEmpty()) categoryList.add(name)
                }
                categoryList.add("Other")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategory.adapter = adapter
            }


        AlertDialog.Builder(this)
            .setTitle("Add Expense")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val dateInput = editTextDate.text.toString().trim()
                val transactionType = editTextTransactionType.text.toString().trim()
                val amountInput = editTextAmount.text.toString().trim()
                val start = editTextStart.text.toString().trim()
                val end = editTextEnd.text.toString().trim()
                val category = spinnerCategory.selectedItem?.toString() ?: "Other"

                if (dateInput.isNotEmpty() && transactionType.isNotEmpty() && amountInput.isNotEmpty()
                    && start.isNotEmpty() && end.isNotEmpty()) {

                    val amount = amountInput.toDoubleOrNull()
                    if (amount == null) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val parsedDate = try {
                        formatter.parse(dateInput)
                    } catch (e: Exception) {
                        null
                    }
                    if (parsedDate == null) {
                        Toast.makeText(this, "Invalid date format (use dd/MM/yyyy)", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val timestamp = Timestamp(parsedDate)

                    if (selectedImageUri != null) {
                        uploadImageAndSaveExpense(
                            selectedImageUri!!, timestamp, transactionType, amount, start, end, category
                        )
                    } else {
                        saveExpense(null, timestamp, transactionType, amount, start, end, category)
                    }
                } else {
                    Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadImageAndSaveExpense(
        imageUri: Uri,
        date: Timestamp,
        transactionType: String,
        amount: Double,
        start: String,
        end: String,
        category: String
    ) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("expenses/$fileName.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveExpense(uri.toString(), date, transactionType, amount, start, end, category)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExpense(
        imageUrl: String?,
        date: Timestamp,
        transactionType: String,
        amount: Double,
        start: String,
        end: String,
        category: String
    ) {
        val expense = hashMapOf(
            "date" to date,
            "transactionType" to transactionType,
            "amount" to amount,
            "start" to start,
            "end" to end,
            "category" to category,
            "imageUrl" to imageUrl
        )

        db.collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                loadExpensesFromFirestore()
                selectedImageUri = null
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadExpensesFromFirestore() {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        if (tableLayout.childCount > 1) {
            tableLayout.removeViews(1, tableLayout.childCount - 1)
        }

        db.collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date: String = try {
                        val timestamp = doc.getTimestamp("date")
                        timestamp?.toDate()?.let { formatter.format(it) } ?: ""
                    } catch (e: Exception) {
                        val dateStr = doc.getString("date")
                        try {
                            val parsedDate = formatter.parse(dateStr ?: "")
                            parsedDate?.let { formatter.format(it) } ?: ""
                        } catch (e: Exception) {
                            ""
                        }
                    }
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
