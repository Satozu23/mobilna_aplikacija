package com.example.komsilukconnect.auth.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.komsilukconnect.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.toObject
interface UserRepository {
    fun getAllUsers(): Flow<List<UserModel>>
    suspend fun getUserById(userId: String): Result<UserModel>
    fun getReviewsForUser(userId: String): Flow<List<ReviewModel>>
    suspend fun addReview(userId: String, rating: Float, comment: String): Result<Unit>
}

class UserRepositoryImpl : UserRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun getAllUsers(): Flow<List<UserModel>> {
        return firestore.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<UserModel>()
            }
    }
    override suspend fun getUserById(userId: String): Result<UserModel> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject<UserModel>()
            if (user != null) {
                Result.Success(user)
            } else {
                Result.Error(Exception("Korisnik nije pronađen."))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getReviewsForUser(userId: String): Flow<List<ReviewModel>> {
        return firestore.collection("users").document(userId).collection("reviews")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects() }
    }

    override suspend fun addReview(userId: String, rating: Float, comment: String): Result<Unit> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.Error(Exception("Morate biti prijavljeni da biste ostavili recenziju."))
        }

        return try {
            val review = ReviewModel(
                authorUid = currentUser.uid,
                authorName = currentUser.displayName ?: currentUser.email ?: "Anonimno",
                rating = rating,
                comment = comment
            )
            firestore.collection("users").document(userId).collection("reviews")
                .add(review).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}