package com.example.pocketmoneypoe

import com.google.firebase.Timestamp


data class Expense(
    val description: String,
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val startTime: String,
    val endTime: String,
    val category: String,
    //val imageUrl: String? = null
)