@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.komsilukconnect.post.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.komsilukconnect.post.data.Applicant
import com.example.komsilukconnect.post.data.PostModel
import com.example.komsilukconnect.post.viewmodel.MyPostsViewModel
import androidx.compose.material3.HorizontalDivider

@Composable
fun MyPostsScreen(
    myPostsViewModel: MyPostsViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val uiState by myPostsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moje objave") },
            actions = {
                IconButton(onClick = {
                    myPostsViewModel.onLogout()
                    onLogout()
                }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Odjavi se")
                }
            }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.myPosts.isEmpty()) {
                Text("Nemate aktivnih objava.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.myPosts) { postItem ->
                        MyPostCard(
                            post = postItem.post,
                            applicants = postItem.applicants,
                            onComplete = { applicant ->
                                myPostsViewModel.completeInteraction(postItem.post, applicant)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyPostCard(
    post: PostModel,
    applicants: List<Applicant>,
    onComplete: (Applicant) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title, style = MaterialTheme.typography.titleLarge)
            Text("Tip: ${post.type}", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            if (applicants.isEmpty()) {
                Text("Niko se još nije javio.")
            } else {
                Text("Prijavljeni korisnici:", fontWeight = FontWeight.Bold)
                applicants.forEach { applicant ->
                    ApplicantItem(applicant = applicant, onComplete = { onComplete(applicant) })
                }
            }
        }
    }
}

@Composable
fun ApplicantItem(
    applicant: Applicant,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(applicant.name ?: "Nepoznat korisnik",
            modifier = Modifier.weight(1f) )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onComplete) {
            Text("Označi kao završeno")
        }
    }
}