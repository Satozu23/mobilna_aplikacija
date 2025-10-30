package com.example.komsilukconnect.ranking.data

import com.example.komsilukconnect.auth.data.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.snapshots

interface RankingRepository {
    fun getUsersRanked(): Flow<List<UserModel>>
}

class RankingRepositoryImpl : RankingRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun getUsersRanked(): Flow<List<UserModel>> {
        return firestore.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { it.toObjects<UserModel>() }
    }
}