package com.example.pocketmoneypoe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pocketmoneypoe.databinding.ActivityProfileBinding
import com.example.pocketmoneypoe.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        lateinit var binding: ActivityProfileBinding

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // Get current user ID
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Set the fetched values to your TextViews
                        binding.nametext.text = document.getString("name") ?: "No name"
                        binding.emailtext.text = document.getString("email") ?: "No email"
                        binding.idtext.text = document.getString("id") ?: "No phone"
                        binding.phonetext.text = document.getString("phone") ?: "No phone"
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackSettings.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}