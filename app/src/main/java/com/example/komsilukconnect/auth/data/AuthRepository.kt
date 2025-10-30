package com.example.komsilukconnect.auth.data

import com.example.komsilukconnect.auth.viewmodel.AuthUiState
import com.example.komsilukconnect.utils.Result
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.GeoPoint
import com.google.android.gms.maps.model.LatLng
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

interface AuthRepository {
    suspend fun registerUser(uiState: AuthUiState, imageUri: Uri?): Result<AuthResult>
    suspend fun loginUser(uiState: AuthUiState): Result<AuthResult>
    suspend fun updateUserLocation(location: LatLng): Result<Unit>
    fun logout()
}

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    override suspend fun registerUser(uiState: AuthUiState, imageUri: Uri?): Result<AuthResult> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(uiState.email, uiState.password).await()
            val userUid = authResult.user!!.uid

            var profilePictureUrl: String? = null
            imageUri?.let {
                val storageRef = storage.reference.child("profile_images/${userUid}.jpg")
                storageRef.putFile(it).await()
                profilePictureUrl = storageRef.downloadUrl.await().toString()
            }

            val userDocumentRef = firestore.collection("users").document(userUid)
            val userModel = UserModel(
                uid = userUid,
                firstName = uiState.firstName,
                lastName = uiState.lastName,
                email = uiState.email,
                phoneNumber = uiState.phoneNumber,
                profilePictureUrl = profilePictureUrl
            )

            userDocumentRef.set(userModel).await()

            Result.Success(authResult)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun loginUser(uiState: AuthUiState): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(uiState.email, uiState.password).await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override suspend fun updateUserLocation(location: LatLng): Result<Unit> {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return Result.Error(Exception("Korisnik nije prijavljen."))
        }
        return try {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            firestore.collection("users").document(uid).update("location", geoPoint).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override fun logout() {
        auth.signOut()
    }
}