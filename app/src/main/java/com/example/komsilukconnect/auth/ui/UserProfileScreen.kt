package com.example.komsilukconnect.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.komsilukconnect.R
import com.example.komsilukconnect.auth.data.ReviewModel
import com.example.komsilukconnect.auth.viewmodel.UserProfileViewModel
import androidx.compose.material.icons.filled.Star
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    userProfileViewModel: UserProfileViewModel = viewModel()
) {
    val uiState by userProfileViewModel.uiState.collectAsState()
    var showAddReviewDialog by remember { mutableStateOf(false) }

    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.user?.firstName ?: "Profil") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Nazad") } }
            )
        },
        floatingActionButton = {
            if (uiState.user?.uid != currentUserUid) {
                FloatingActionButton(onClick = { showAddReviewDialog = true }) {
                    Icon(Icons.Default.Star, contentDescription = "Dodaj recenziju")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.user != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        ProfileHeader(user = uiState.user!!)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Recenzije", style = MaterialTheme.typography.titleLarge)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(uiState.reviews) { review ->
                        ReviewItem(review = review)
                    }
                }
            } else {
                Text(uiState.errorMessage ?: "Došlo je do greške.")
            }
        }
    }

    if (showAddReviewDialog) {
        AddReviewDialog(
            onDismiss = { showAddReviewDialog = false },
            onConfirm = { rating, comment ->
                userProfileViewModel.addReview(rating, comment)
                showAddReviewDialog = false
            }
        )
    }
}

@Composable
fun ProfileHeader(user: com.example.komsilukconnect.auth.data.UserModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = user.profilePictureUrl ?: R.drawable.ic_launcher_background),
            contentDescription = "Profilna slika",
            modifier = Modifier.size(120.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("${user.firstName} ${user.lastName}", style = MaterialTheme.typography.headlineSmall)
        Text("${user.points} poena", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ReviewItem(review: ReviewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(review.authorName, fontWeight = FontWeight.Bold)
            Text("Ocena: ${review.rating}/5.0")
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.comment)
        }
    }
}

@Composable
fun AddReviewDialog(onDismiss: () -> Unit, onConfirm: (Float, String) -> Unit) {
    var rating by remember { mutableFloatStateOf(3f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ostavite recenziju") },
        text = {
            Column {
                Text("Ocena: ${rating.toInt()}")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentar") }
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(rating, comment) }) { Text("Potvrdi") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Otkaži") } }
    )
}