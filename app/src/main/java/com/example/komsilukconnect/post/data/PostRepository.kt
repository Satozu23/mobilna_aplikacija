package com.example.komsilukconnect.post.data

import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.utils.Result
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.flowOf


private data class GeoBoundingBox(
    val northEast: LatLng,
    val southWest: LatLng
)
data class Applicant(
    val uid: String = "",
    val name: String? = null,
)

interface PostRepository {
    suspend fun createPost(title: String, description: String, type: PostType, durationHours: Int, location: GeoPoint): Result<Unit>
    fun getAllPosts(): Flow<List<PostModel>>
    suspend fun getPostById(postId: String): Result<PostModel>
    fun getNearbyPosts(center: LatLng, radiusInKm: Double): Flow<List<PostModel>>
    suspend fun applyToPost(post: PostModel): Result<Unit>
    fun getPostsByCurrentUser(): Flow<List<PostModel>>
    fun getApplicantsForPost(postId: String): Flow<List<Applicant>>
    suspend fun completeInteraction(post: PostModel, applicant: Applicant): Result<Unit>
}

class PostRepositoryImpl : PostRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun createPost(title: String, description: String, type: PostType, durationHours: Int, location: GeoPoint): Result<Unit> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.Error(Exception("Korisnik nije prijavljen."))
        }

        return try {
            val userDocumentRef = firestore.collection("users").document(currentUser.uid)
            val userDocument = userDocumentRef.get().await()
            val userModel = userDocument.toObject<UserModel>()

            val authorName = if (userModel != null) {
                "${userModel.firstName} ${userModel.lastName}"
            } else {
                currentUser.email ?: "Nepoznat autor"
            }

            val postCollection = firestore.collection("posts")
            val newPostRef = postCollection.document()
            val postId = newPostRef.id

            val newPost = PostModel(
                id = postId,
                authorUid = currentUser.uid,
                authorName = authorName,
                title = title,
                description = description,
                type = type,
                location = location,
                durationHours = durationHours
            )

            firestore.runBatch { batch ->
                batch.set(newPostRef, newPost)
                batch.update(userDocumentRef, "points", FieldValue.increment(10))
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getAllPosts(): Flow<List<PostModel>> {
        return firestore.collection("posts")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    val post = document.toObject<PostModel>()
                    post?.copy(id = document.id)
                }
            }
    }

    override suspend fun getPostById(postId: String): Result<PostModel> {
        return try {
            val document = firestore.collection("posts").document(postId).get().await()
            val post = document.toObject<PostModel>()
            if (post != null) {
                Result.Success(post.copy(id = document.id))
            } else {
                Result.Error(Exception("Objava nije pronađena."))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getNearbyPosts(center: LatLng, radiusInKm: Double): Flow<List<PostModel>> {
        val boundingBox = getBoundingBox(center, radiusInKm)

        val query = firestore.collection("posts")
            .whereGreaterThan("location", GeoPoint(boundingBox.southWest.latitude, boundingBox.southWest.longitude))
            .whereLessThan("location", GeoPoint(boundingBox.northEast.latitude, boundingBox.northEast.longitude))

        return query.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { document ->
                val post = document.toObject<PostModel>()
                post?.copy(id = document.id)
            }
        }
    }

    private fun getBoundingBox(center: LatLng, radiusInKm: Double): GeoBoundingBox {
        val earthRadiusKm = 6371.0
        val latRadian = Math.toRadians(center.latitude)
        val lonRadian = Math.toRadians(center.longitude)

        val radiusOnEarth = radiusInKm / earthRadiusKm

        val minLat = Math.toDegrees(latRadian - radiusOnEarth)
        val maxLat = Math.toDegrees(latRadian + radiusOnEarth)

        val deltaLon = asin(sin(radiusOnEarth) / cos(latRadian))

        val minLon = Math.toDegrees(lonRadian - deltaLon)
        val maxLon = Math.toDegrees(lonRadian + deltaLon)

        return GeoBoundingBox(
            northEast = LatLng(maxLat, maxLon),
            southWest = LatLng(minLat, minLon)
        )
    }
    override suspend fun applyToPost(post: PostModel): Result<Unit> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return Result.Error(Exception("Morate biti prijavljeni."))
        }

        if (currentUser.uid == post.authorUid) {
            return Result.Error(Exception("Ne možete se javiti na sopstvenu objavu."))
        }

        return try {
            val applicantData = mapOf(
                "uid" to currentUser.uid,
                "name" to (currentUser.displayName ?: currentUser.email),
                "appliedAt" to FieldValue.serverTimestamp()
            )

            val applicantRef = firestore.collection("posts").document(post.id)
                .collection("applicants").document(currentUser.uid)

            applicantRef.set(applicantData).await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    override fun getPostsByCurrentUser(): Flow<List<PostModel>> {
        val currentUserUid = auth.currentUser?.uid ?: return flowOf(emptyList())

        return firestore.collection("posts")
            .whereEqualTo("authorUid", currentUserUid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { document ->
                    val post = document.toObject<PostModel>()
                    post?.copy(id = document.id)
                }
            }
    }

    override fun getApplicantsForPost(postId: String): Flow<List<Applicant>> {
        return firestore.collection("posts").document(postId).collection("applicants")
            .snapshots()
            .map { it.toObjects() }
    }

    override suspend fun completeInteraction(post: PostModel, applicant: Applicant): Result<Unit> {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid != post.authorUid) {
            return Result.Error(Exception("Samo autor objave može da je završi."))
        }

        return try {
            val postRef = firestore.collection("posts").document(post.id)

            val userToRewardRef = if (post.type == PostType.REQUEST) {
                firestore.collection("users").document(applicant.uid)
            } else {
                firestore.collection("users").document(post.authorUid)
            }

            firestore.runBatch { batch ->
                batch.update(userToRewardRef, "points", FieldValue.increment(5))
                batch.delete(postRef)
            }.await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
