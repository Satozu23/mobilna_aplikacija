package com.example.komsilukconnect.post.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.komsilukconnect.post.viewmodel.PostDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.komsilukconnect.post.viewmodel.PostDetailsEvent
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    onNavigateBack: () -> Unit,
    postDetailsViewModel: PostDetailsViewModel = viewModel()
) {
    val uiState by postDetailsViewModel.uiState.collectAsState()
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = uiState.applySuccess) {
        if (uiState.applySuccess) {
            snackbarHostState.showSnackbar(
                message = "Uspešno ste se prijavili na objavu!",
                duration = SnackbarDuration.Short
            )
            postDetailsViewModel.onEvent(PostDetailsEvent.SuccessMessageShown)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.post?.title ?: "Detalji objave") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.errorMessage != null) {
                Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (uiState.post != null) {
                val post = uiState.post!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(post.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Autor: ${post.authorName}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    post.createdAt?.let {
                        Text("Objavljeno: ${formatDate(it)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(post.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.weight(1f))

                    if (post.authorUid != currentUserUid) {
                        Button(
                            onClick = { postDetailsViewModel.onEvent(PostDetailsEvent.ApplyToPost) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("Javi se")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy. 'u' HH:mm", Locale.getDefault())
    return formatter.format(date)
}