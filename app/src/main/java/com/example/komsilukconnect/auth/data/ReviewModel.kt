package com.example.komsilukconnect.auth.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReviewModel(
    val authorUid: String = "",
    val authorName: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)