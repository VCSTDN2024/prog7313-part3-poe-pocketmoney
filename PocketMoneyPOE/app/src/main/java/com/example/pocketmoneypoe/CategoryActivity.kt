package com.example.pocketmoneypoe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CategoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        recyclerView = findViewById(R.id.recyclerViewCategories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val buttonAddCategory: Button = findViewById(R.id.buttonAddCategoryToFirestore)
        val editTextCategoryName: EditText = findViewById(R.id.editTextCategoryName)

        buttonAddCategory.setOnClickListener {
            val categoryName = editTextCategoryName.text.toString().trim()

            if (categoryName.isNotEmpty()) {
                // Add the category to Firestore
                val categoryMap = hashMapOf("name" to categoryName)

                db.collection("categories")
                    .add(categoryMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category Added!", Toast.LENGTH_SHORT).show()
                        editTextCategoryName.text.clear()
                        loadCategories()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add category: $e", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        loadCategories()
    }

    private fun loadCategories() {
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = mutableListOf<Category>()
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    categories.add(Category(name, listOf()))
                }

                categoryAdapter = CategoryAdapter(categories)
                recyclerView.adapter = categoryAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: $e", Toast.LENGTH_SHORT).show()
            }
    }
}