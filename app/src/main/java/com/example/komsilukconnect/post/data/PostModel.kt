package com.example.komsilukconnect.post.data

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class PostType {
    OFFER,
    REQUEST
}

data class PostModel(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val type: PostType = PostType.REQUEST,
    val location: GeoPoint? = null,
    val imageUrl: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val durationHours: Int = 2
)