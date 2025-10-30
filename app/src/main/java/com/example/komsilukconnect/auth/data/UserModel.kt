package com.example.komsilukconnect.auth.data
import com.google.firebase.firestore.GeoPoint

data class UserModel(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String? = null,
    val points: Int = 0,
    val location: GeoPoint? = null
)