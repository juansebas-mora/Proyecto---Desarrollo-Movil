package com.example.urumbox.data.model

import com.google.firebase.Timestamp

data class AccessHistoryItem(
    val id: String = "",
    val userId: String = "",
    val zona: String = "",
    val timestamp: Timestamp? = null,
    val registradoPor: String = ""
)
